package org.zhinanzhen.tb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zhinanzhen.b.service.WXWorkService;
import org.zhinanzhen.tb.service.WeComDirectoryService;
import org.zhinanzhen.tb.utils.WXWorkAPI;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WeComDirectoryServiceImpl implements WeComDirectoryService {

    private static final long TOKEN_EXPIRY_SAFETY_MILLIS = 300000L;

    @Resource
    private WXWorkService wxWorkService;

    @Autowired
    private RestTemplate restTemplate;

    private final Object corpTokenLock = new Object();
    private final Object customerTokenLock = new Object();
    private volatile CachedToken corpToken;
    private volatile CachedToken customerToken;

    @Override
    public JSONArray listDepartments() throws Exception {
        String token = getCorpToken();
        String url = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token="
                + urlEncode(token);
        JSONObject response = getJson(url);
        checkWeComResponse("department/list", response);
        JSONArray departments = response.getJSONArray("department");
        return departments == null ? new JSONArray() : departments;
    }

    @Override
    public JSONArray listEmployees(long departmentId) throws Exception {
        String token = getCorpToken();
        String url = "https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token="
                + urlEncode(token)
                + "&department_id=" + departmentId
                + "&fetch_child=0";
        JSONObject response = getJson(url);
        checkWeComResponse("user/simplelist", response);
        JSONArray users = response.getJSONArray("userlist");
        JSONArray result = new JSONArray();
        for (int i = 0; users != null && i < users.size(); i++) {
            JSONObject source = users.getJSONObject(i);
            String userId = source.getString("userid");
            if (isBlank(userId)) {
                continue;
            }
            JSONObject employee = new JSONObject();
            employee.put("weComUserId", userId);
            employee.put("name", source.getString("name"));
            employee.put("departmentIds", source.get("department"));
            result.add(employee);
        }
        return result;
    }

    @Override
    public JSONArray listCustomers(String weComUserId) throws Exception {
        String token = getCustomerToken();
        Map<String, JSONObject> customersById = new LinkedHashMap<>();
        String cursor = "";
        int pageNumber = 0;
        do {
            Map<String, Object> responseMap =
                    wxWorkService.getexternalContactList(
                            token, weComUserId, cursor, 100);
            JSONObject response = JSONObject.parseObject(
                    JSON.toJSONString(responseMap));
            checkWeComResponse("externalcontact/batch/get_by_user", response);
            JSONArray customerList = response.getJSONArray("external_contact_list");
            for (int i = 0; customerList != null && i < customerList.size(); i++) {
                JSONObject relation = customerList.getJSONObject(i);
                JSONObject externalContact = relation.getJSONObject("external_contact");
                if (externalContact == null) {
                    continue;
                }
                String externalUserId = externalContact.getString("external_userid");
                if (isBlank(externalUserId) || customersById.containsKey(externalUserId)) {
                    continue;
                }
                JSONObject customer = new JSONObject();
                customer.put("externalUserId", externalUserId);
                customer.put("name", isBlank(externalContact.getString("name"))
                        ? externalUserId : externalContact.getString("name"));
                customer.put("avatar", externalContact.getString("avatar"));
                JSONObject followInfo = relation.getJSONObject("follow_info");
                customer.put("createdAtEpochMillis",
                        followInfo == null || followInfo.getLong("createtime") == null
                                ? null : followInfo.getLongValue("createtime") * 1000L);
                customersById.put(externalUserId, customer);
            }
            cursor = response.getString("next_cursor");
            pageNumber++;
        } while (!isBlank(cursor) && pageNumber < 100);

        JSONArray result = new JSONArray();
        result.addAll(customersById.values());
        return result;
    }

    private String getCorpToken() {
        CachedToken token = corpToken;
        if (isValid(token)) {
            return token.value;
        }
        synchronized (corpTokenLock) {
            if (!isValid(corpToken)) {
                corpToken = requestToken(WXWorkAPI.SECRET_CORP);
            }
            return corpToken.value;
        }
    }

    private String getCustomerToken() {
        CachedToken token = customerToken;
        if (isValid(token)) {
            return token.value;
        }
        synchronized (customerTokenLock) {
            if (!isValid(customerToken)) {
                customerToken = requestToken(WXWorkAPI.SECRET_CUSTOMER);
            }
            return customerToken.value;
        }
    }

    private CachedToken requestToken(String secret) {
        Map<String, Object> response = wxWorkService.getToken(secret);
        Object errorCode = response.get("errcode");
        if (errorCode == null || Integer.parseInt(String.valueOf(errorCode)) != 0) {
            throw new IllegalStateException("gettoken response error, errcode="
                    + errorCode + ", errmsg=" + response.get("errmsg"));
        }
        String value = String.valueOf(response.get("access_token"));
        if (isBlank(value) || "null".equals(value)) {
            throw new IllegalStateException("gettoken returned an empty access_token");
        }
        long expiresIn = 7200L;
        if (response.get("expires_in") != null) {
            expiresIn = Long.parseLong(String.valueOf(response.get("expires_in")));
        }
        return new CachedToken(value, System.currentTimeMillis()
                + Math.max(60000L, expiresIn * 1000L - TOKEN_EXPIRY_SAFETY_MILLIS));
    }

    private JSONObject getJson(String url) {
        return JSONObject.parseObject(restTemplate.getForObject(url, String.class));
    }

    private static void checkWeComResponse(String operation, JSONObject response) {
        if (response == null) {
            throw new IllegalStateException(operation + " returned an empty response");
        }
        int errorCode = response.getIntValue("errcode");
        if (errorCode != 0) {
            throw new IllegalStateException(operation + " response error, errcode="
                    + errorCode + ", errmsg=" + response.getString("errmsg"));
        }
    }

    private static boolean isValid(CachedToken token) {
        return token != null && !isBlank(token.value)
                && System.currentTimeMillis() < token.expiresAtMillis;
    }

    private static String urlEncode(String value) throws Exception {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class CachedToken {
        private final String value;
        private final long expiresAtMillis;

        private CachedToken(String value, long expiresAtMillis) {
            this.value = value;
            this.expiresAtMillis = expiresAtMillis;
        }
    }
}
