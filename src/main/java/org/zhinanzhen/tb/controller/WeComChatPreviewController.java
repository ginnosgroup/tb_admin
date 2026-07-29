package org.zhinanzhen.tb.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.zhinanzhen.tb.service.WeComChatArchiveService;
import org.zhinanzhen.tb.service.WeComDirectoryService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;

/**
 * Supplies the short-lived agentConfig signature used by the temporary
 * ww-open-message chat preview page.
 */
@Controller
@RequestMapping("/wecom/chat-preview")
@Slf4j
public class WeComChatPreviewController extends BaseController {

    private static final long TICKET_EXPIRY_SAFETY_SECONDS = 300L;
    private static final String QUERY_TYPE_CUSTOMER = "CUSTOMER";
    private static final String QUERY_TYPE_GROUP = "GROUP";

    @Autowired
    private RestTemplate restTemplate;

    @Resource
    private WeComDirectoryService weComDirectoryService;

    @Resource
    private WeComChatArchiveService weComChatArchiveService;

    @Value("${spring.social.wecom.app-id}")
    private String corpId;

    @Value("${spring.social.wecom.agent-id}")
    private String agentId;

    @Value("${spring.social.wecom.app-secret}")
    private String applicationSecret;

    @Value("${wecom.chat.preview.environment:production}")
    private String previewEnvironment;

    @Value("${wecom.chat.preview.production.page-url:"
            + "https://znzapi.cn/webroot_new/wecom-chat-preview.html}")
    private String productionPageUrl;

    @Value("${wecom.chat.preview.test.page-url:"
            + "http://test.znzapi.cn/webroot_new/wecom-chat-preview.html}")
    private String testPageUrl;

    private final Object ticketLock = new Object();
    private volatile String cachedAgentConfigTicket;
    private volatile long cachedTicketExpiresAtEpochSeconds;

    @GetMapping("/agent-config")
    @ResponseBody
    public Response<JSONObject> getAgentConfig(
            @RequestParam("url") String pageUrl, HttpServletRequest request) {
        String accessError = getSuperAdminAccessError(request);
        if (accessError != null) {
            return new Response<>(1, accessError);
        }

        try {
            String configuredPageUrl = getConfiguredPageUrl();
            String normalizedPageUrl = removeFragment(pageUrl);
            if (!isSamePage(configuredPageUrl, normalizedPageUrl)) {
                return new Response<>(1, "当前页面不属于已配置的企业微信会话预览地址，"
                        + "当前环境：" + normalizeEnvironment(previewEnvironment));
            }

            String ticket = getAgentConfigTicket();
            long timestamp = System.currentTimeMillis() / 1000L;
            String nonceStr = UUID.randomUUID().toString().replace("-", "");

            JSONObject data = new JSONObject();
            data.put("environment", normalizeEnvironment(previewEnvironment));
            data.put("pageUrl", configuredPageUrl);
            data.put("corpId", corpId);
            data.put("agentId", agentId);
            data.put("timestamp", timestamp);
            data.put("nonceStr", nonceStr);
            data.put("signature",
                    createJsApiSignature(ticket, nonceStr, timestamp, normalizedPageUrl));
            data.put("expiresAtEpochSeconds", cachedTicketExpiresAtEpochSeconds);
            return new Response<>(0, "获取企业微信签名成功", data);
        } catch (Exception ex) {
            log.error("Unable to create WeCom chat preview agentConfig signature", ex);
            return new Response<>(1, "获取企业微信签名失败：" + ex.getMessage());
        }
    }

    @GetMapping("/departments")
    @ResponseBody
    public Response<JSONArray> listDepartments(HttpServletRequest request) {
        String accessError = getSuperAdminAccessError(request);
        if (accessError != null) {
            return new Response<>(1, accessError);
        }
        try {
            return new Response<>(0, "查询企业微信部门成功",
                    weComDirectoryService.listDepartments());
        } catch (Exception ex) {
            log.error("Unable to list WeCom departments", ex);
            return new Response<>(1, "查询企业微信部门失败：" + ex.getMessage());
        }
    }

    @GetMapping("/employees")
    @ResponseBody
    public Response<JSONArray> listEmployees(
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            HttpServletRequest request) {
        String accessError = getSuperAdminAccessError(request);
        if (accessError != null) {
            return new Response<>(1, accessError);
        }
        if (departmentId == null || departmentId <= 0) {
            return new Response<>(1,
                    "请选择企业微信部门；如果页面未显示部门下拉框，请更新并刷新前端文件");
        }
        try {
            return new Response<>(0, "查询企业人员成功",
                    weComDirectoryService.listEmployees(departmentId));
        } catch (Exception ex) {
            log.error("Unable to list WeCom employees, departmentId={}",
                    departmentId, ex);
            return new Response<>(1, "查询企业人员失败：" + ex.getMessage());
        }
    }

    @GetMapping("/customers")
    @ResponseBody
    public Response<JSONArray> listCustomers(
            @RequestParam("weComUserId") String weComUserId,
            HttpServletRequest request) {
        String accessError = getSuperAdminAccessError(request);
        if (accessError != null) {
            return new Response<>(1, accessError);
        }
        if (isBlank(weComUserId)) {
            return new Response<>(1, "请选择企业人员");
        }
        try {
            return new Response<>(0, "查询该人员添加的客户成功",
                    weComDirectoryService.listCustomers(weComUserId));
        } catch (Exception ex) {
            log.error("Unable to list WeCom customers, weComUserId={}",
                    weComUserId, ex);
            return new Response<>(1, "查询客户失败：" + ex.getMessage());
        }
    }

    @GetMapping("/groups")
    @ResponseBody
    public Response<JSONArray> listEmployeeGroups(
            @RequestParam("employeeUserId") String employeeUserId,
            HttpServletRequest request) {
        String accessError = getSuperAdminAccessError(request);
        if (accessError != null) {
            return new Response<>(1, accessError);
        }
        if (isBlank(employeeUserId)) {
            return new Response<>(1, "请选择企业人员");
        }
        try {
            return new Response<>(0, "查询该人员参与的群聊成功",
                    weComChatArchiveService.listEmployeeGroupChatIds(employeeUserId));
        } catch (Exception ex) {
            log.error("Unable to list WeCom group chats, employeeUserId={}",
                    employeeUserId, ex);
            return new Response<>(1, "查询群聊失败：" + ex.getMessage());
        }
    }

    @GetMapping("/archive-status")
    @ResponseBody
    public Response<JSONObject> getArchiveStatus(HttpServletRequest request) {
        String accessError = getSuperAdminAccessError(request);
        if (accessError != null) {
            return new Response<>(1, accessError);
        }
        try {
            return new Response<>(0, "查询会话存档同步状态成功",
                    weComChatArchiveService.getArchiveStatus());
        } catch (Exception ex) {
            log.error("Unable to get WeCom chat archive status", ex);
            return new Response<>(1, "查询会话存档同步状态失败：" + ex.getMessage());
        }
    }

    @PostMapping("/sync")
    @ResponseBody
    public Response<JSONObject> syncArchive(HttpServletRequest request) {
        String accessError = getSuperAdminAccessError(request);
        if (accessError != null) {
            return new Response<>(1, accessError);
        }
        try {
            return new Response<>(0, "企业微信会话存档同步完成",
                    weComChatArchiveService.syncNow());
        } catch (Exception ex) {
            log.error("Unable to sync WeCom chat archive", ex);
            return new Response<>(1, "企业微信会话存档同步失败：" + ex.getMessage());
        }
    }

    @GetMapping("/messages")
    @ResponseBody
    public Response<JSONObject> queryMessages(
            @RequestParam("employeeUserId") String employeeUserId,
            @RequestParam(value = "externalUserId", required = false)
                    String externalUserId,
            @RequestParam(value = "chatId", required = false)
                    String chatId,
            @RequestParam(value = "queryType", defaultValue = QUERY_TYPE_CUSTOMER)
                    String queryType,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "200") int pageSize,
            HttpServletRequest request) {
        String accessError = getSuperAdminAccessError(request);
        if (accessError != null) {
            return new Response<>(1, accessError);
        }
        String normalizedQueryType = normalizeQueryType(queryType);
        if (normalizedQueryType == null) {
            return new Response<>(1, "queryType 只允许 CUSTOMER 或 GROUP");
        }
        if (isBlank(employeeUserId)) {
            return new Response<>(1, "请选择企业人员");
        }
        if (QUERY_TYPE_CUSTOMER.equals(normalizedQueryType)
                && isBlank(externalUserId)) {
            return new Response<>(1, "请选择该人员添加的客户");
        }
        if (QUERY_TYPE_GROUP.equals(normalizedQueryType) && isBlank(chatId)) {
            return new Response<>(1, "请选择群聊 ID");
        }
        if (pageNum < 0 || pageSize < 1 || pageSize > 500) {
            return new Response<>(1, "pageNum 不能小于 0，pageSize 必须在 1 到 500 之间");
        }
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            if (end.isBefore(start)) {
                return new Response<>(1, "结束日期不能早于开始日期");
            }
            ZoneId zoneId = ZoneId.systemDefault();
            long startTime = start.atStartOfDay(zoneId).toInstant().toEpochMilli();
            long endTime = end.plusDays(1).atStartOfDay(zoneId)
                    .toInstant().toEpochMilli();
            JSONObject data = QUERY_TYPE_GROUP.equals(normalizedQueryType)
                    ? weComChatArchiveService.queryEmployeeGroupMessages(
                            employeeUserId, chatId,
                            startTime, endTime, pageNum, pageSize)
                    : weComChatArchiveService.queryMessages(
                            employeeUserId, externalUserId,
                            startTime, endTime, pageNum, pageSize);
            data.put("queryType", normalizedQueryType);
            return new Response<>(0, "查询企业微信会话记录成功", data);
        } catch (Exception ex) {
            log.error("Unable to query WeCom chat archive, employeeUserId={}, "
                            + "externalUserId={}, chatId={}, queryType={}, "
                            + "startDate={}, endDate={}",
                    employeeUserId, externalUserId, chatId, normalizedQueryType,
                    startDate, endDate, ex);
            return new Response<>(1, "查询企业微信会话记录失败：" + ex.getMessage());
        }
    }

    private static String normalizeQueryType(String queryType) {
        if (queryType == null) {
            return QUERY_TYPE_CUSTOMER;
        }
        String value = queryType.trim().toUpperCase(Locale.ROOT);
        return QUERY_TYPE_CUSTOMER.equals(value) || QUERY_TYPE_GROUP.equals(value)
                ? value : null;
    }

    private String getSuperAdminAccessError(HttpServletRequest request) {
        AdminUserLoginInfo loginInfo = getAdminUserLoginInfo(request);
        if (loginInfo == null) {
            return "未登录，请先登录后台系统";
        }
        if (!"SUPERAD".equalsIgnoreCase(loginInfo.getApList())) {
            return "只有超级管理员可以查看会话记录";
        }
        return null;
    }

    private String getConfiguredPageUrl() {
        String environment = normalizeEnvironment(previewEnvironment);
        if ("production".equals(environment)) {
            return requirePageUrl("wecom.chat.preview.production.page-url", productionPageUrl);
        }
        if ("test".equals(environment)) {
            return requirePageUrl("wecom.chat.preview.test.page-url", testPageUrl);
        }
        throw new IllegalArgumentException(
                "不支持的企业微信会话预览环境：" + previewEnvironment
                        + "，只允许 production 或 test");
    }

    static String normalizeEnvironment(String environment) {
        if (environment == null) {
            return "";
        }
        String value = environment.trim().toLowerCase(Locale.ROOT);
        if ("prod".equals(value)) {
            return "production";
        }
        return value;
    }

    static boolean isSamePage(String configuredPageUrl, String requestedPageUrl)
            throws URISyntaxException {
        URI configured = parsePageUri(configuredPageUrl);
        URI requested = parsePageUri(requestedPageUrl);
        return configured.getScheme().equalsIgnoreCase(requested.getScheme())
                && configured.getHost().equalsIgnoreCase(requested.getHost())
                && effectivePort(configured) == effectivePort(requested)
                && normalizedRawPath(configured).equals(normalizedRawPath(requested));
    }

    private static String requirePageUrl(String propertyName, String pageUrl) {
        if (isBlank(pageUrl)) {
            throw new IllegalArgumentException(propertyName + "不能为空");
        }
        try {
            parsePageUri(pageUrl);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(propertyName + "不是合法地址：" + pageUrl, ex);
        }
        return removeFragment(pageUrl);
    }

    private static URI parsePageUri(String pageUrl) throws URISyntaxException {
        String value = removeFragment(pageUrl);
        URI uri = new URI(value);
        if (!uri.isAbsolute() || isBlank(uri.getHost()) || uri.getUserInfo() != null) {
            throw new URISyntaxException(value, "页面地址必须是包含域名的绝对地址");
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new URISyntaxException(value, "页面地址只允许使用 HTTP 或 HTTPS");
        }
        return uri;
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private static String normalizedRawPath(URI uri) {
        String path = uri.getRawPath();
        return isBlank(path) ? "/" : path;
    }

    private String getAgentConfigTicket() throws Exception {
        long now = System.currentTimeMillis() / 1000L;
        if (isTicketValid(now)) {
            return cachedAgentConfigTicket;
        }

        synchronized (ticketLock) {
            now = System.currentTimeMillis() / 1000L;
            if (isTicketValid(now)) {
                return cachedAgentConfigTicket;
            }

            String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="
                    + urlEncode(corpId) + "&corpsecret=" + urlEncode(applicationSecret);
            JSONObject tokenResponse = JSONObject.parseObject(
                    restTemplate.getForObject(tokenUrl, String.class));
            checkWeComResponse("gettoken", tokenResponse);
            String accessToken = tokenResponse.getString("access_token");
            if (isBlank(accessToken)) {
                throw new IllegalStateException("gettoken returned an empty access_token");
            }

            String ticketUrl = "https://qyapi.weixin.qq.com/cgi-bin/ticket/get?access_token="
                    + urlEncode(accessToken) + "&type=agent_config";
            JSONObject ticketResponse = JSONObject.parseObject(
                    restTemplate.getForObject(ticketUrl, String.class));
            checkWeComResponse("get agent_config jsapi_ticket", ticketResponse);
            String ticket = ticketResponse.getString("ticket");
            if (isBlank(ticket)) {
                throw new IllegalStateException(
                        "get agent_config jsapi_ticket returned an empty ticket");
            }

            long expiresIn = ticketResponse.getLongValue("expires_in");
            cachedAgentConfigTicket = ticket;
            cachedTicketExpiresAtEpochSeconds = now + Math.max(
                    1L, expiresIn - TICKET_EXPIRY_SAFETY_SECONDS);
            return cachedAgentConfigTicket;
        }
    }

    private boolean isTicketValid(long nowEpochSeconds) {
        return !isBlank(cachedAgentConfigTicket)
                && nowEpochSeconds < cachedTicketExpiresAtEpochSeconds;
    }

    private static String createJsApiSignature(
            String ticket, String nonceStr, long timestamp, String pageUrl) throws Exception {
        String value = "jsapi_ticket=" + ticket
                + "&noncestr=" + nonceStr
                + "&timestamp=" + timestamp
                + "&url=" + pageUrl;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder(digest.length * 2);
        for (byte item : digest) {
            result.append(String.format("%02x", item & 0xff));
        }
        return result.toString();
    }

    private static void checkWeComResponse(String operation, JSONObject response) {
        if (response == null) {
            throw new IllegalStateException(operation + " returned an empty JSON response");
        }
        int errorCode = response.getIntValue("errcode");
        if (errorCode != 0) {
            throw new IllegalStateException(operation + " response error, errcode=" + errorCode
                    + ", errmsg=" + response.getString("errmsg"));
        }
    }

    private static String removeFragment(String url) {
        if (url == null) {
            return "";
        }
        String value = url.trim();
        int fragmentIndex = value.indexOf('#');
        return fragmentIndex < 0 ? value : value.substring(0, fragmentIndex);
    }

    private static String urlEncode(String value) throws Exception {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
