package org.zhinanzhen.tb.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.social.wecom.app-id}")
    private String corpId;

    @Value("${spring.social.wecom.agent-id}")
    private String agentId;

    @Value("${spring.social.wecom.app-secret}")
    private String applicationSecret;

    @Value("${wecom.chat.preview.page-url:"
            + "https://yongjinbiao.zhinanzhen.org/webroot_new/wecom-chat-preview.html}")
    private String allowedPageUrl;

    private final Object ticketLock = new Object();
    private volatile String cachedAgentConfigTicket;
    private volatile long cachedTicketExpiresAtEpochSeconds;

    @GetMapping("/agent-config")
    @ResponseBody
    public Response<JSONObject> getAgentConfig(
            @RequestParam("url") String pageUrl, HttpServletRequest request) {
        AdminUserLoginInfo loginInfo = getAdminUserLoginInfo(request);
        if (loginInfo == null) {
            return new Response<>(1, "未登录，请先登录后台系统");
        }
        if (!"SUPERAD".equalsIgnoreCase(loginInfo.getApList())) {
            return new Response<>(1, "只有超级管理员可以查看会话记录");
        }

        String normalizedPageUrl = removeFragment(pageUrl);
        if (!removeFragment(allowedPageUrl).equals(normalizedPageUrl)) {
            return new Response<>(1, "不允许为当前页面地址生成企业微信签名");
        }

        try {
            String ticket = getAgentConfigTicket();
            long timestamp = System.currentTimeMillis() / 1000L;
            String nonceStr = UUID.randomUUID().toString().replace("-", "");

            JSONObject data = new JSONObject();
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
