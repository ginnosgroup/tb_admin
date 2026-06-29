package org.zhinanzhen.tb.utils;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataListUtils {

    public final static String ADD_CLOUDDISKFILE = "http://127.0.0.1:8001/admin_v2.1/externalInterface/addCloudDiskFile";
    public final static String GET_CLOUDDISKFILE = "http://127.0.0.1:8001/admin_v2.1/externalInterface/getCloudDiskFileById?";
    public final static String UPDATE_CLOUDDISKFILE = "http://127.0.0.1:8001/admin_v2.1/externalInterface/updateCloudDiskFile";
    public final static String LIST_CLOUDDISKFILE = "http://127.0.0.1:8001/admin_v2.1/externalInterface/listByParentFileId?";
    public final static String GET_USERBYNAME = "http://127.0.0.1:8001/admin_v2.1/externalInterface/getUserByName?";
    public final static String GET_ADVISERBYID = "http://127.0.0.1:8001/admin_v2.1/externalInterface/getAdviserById?";
    public final static String GET_OFFICIALBYID = "http://127.0.0.1:8001/admin_v2.1/externalInterface/getOfficialById?";
    public final static String GET_ADMINUSERBYUSERNAME = "http://127.0.0.1:8001/admin_v2.1/externalInterface/getAdminuserByUserName?";
    public final static String GET_CLOUDDISK = "http://127.0.0.1:8001/admin_v2.1/externalInterface/getCloudDisk?";
    public final static String LIST_BYRELATIVEPATH = "http://127.0.0.1:8001/admin_v2.1/externalInterface/getCloudDisk?";

    private static final String LOGIN_URL = "http://127.0.0.1:8001/admin_v2.1/admin_user/login";

    // 缓存 JSESSIONID，避免每次请求都登录，防止后端 Session 堆积内存溢出
    private static volatile String cachedJsessionId;
    private static volatile String cachedLoginParams;

    // GET请求发送
    public static JSONObject sendGet(String url) {
        JSONObject jsonss = null;
        BufferedReader in = null;
        try {
            url = encodeParameterValues(url);
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.connect();

            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8")
            );

            String line;

            StringBuilder result = new StringBuilder();
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

            if (result.length() > 0) {
                jsonss = JSONObject.parseObject(result.toString());
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return jsonss;
    }

    // POST请求发送
    public static JSONObject sendPost(String url, String params, StringBuilder formData) {
        JSONObject jsonss = null;
        String jsessionid = getJsessionId(params);
        if (jsessionid == null) {
            return null;
        }

        try {
            String strRead = null;

            URL realUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cookie", jsessionid);

            try (OutputStream os = connection.getOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                writer.write(formData.toString());
                writer.flush();
            }

            int responseCode = connection.getResponseCode();

            // Session 过期 → 重新登录重试一次
            if (responseCode == 401 || responseCode == 403) {
                cachedJsessionId = null;
                jsessionid = getJsessionId(params);
                if (jsessionid != null) {
                    connection.disconnect();
                    connection = (HttpURLConnection) realUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Cookie", jsessionid);
                    try (OutputStream os = connection.getOutputStream();
                         OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                        writer.write(formData.toString());
                        writer.flush();
                    }
                    responseCode = connection.getResponseCode();
                }
            }

            if (responseCode == 200) {
                try (InputStream is = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    while ((strRead = reader.readLine()) != null) {
                        jsonss = JSONObject.parseObject(strRead);
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonss;
    }

    /**
     * 获取 JSESSIONID（带缓存，避免每次请求都登录导致后端 Session 堆积）
     */
    private static String getJsessionId(String params) {
        if (cachedJsessionId != null && params.equals(cachedLoginParams)) {
            return cachedJsessionId;
        }

        try {
            URL urlT = new URL(LOGIN_URL);
            HttpURLConnection conn = (HttpURLConnection) urlT.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                conn.disconnect();
                return null;
            }

            String cookieHeader = conn.getHeaderField("Set-Cookie");
            if (cookieHeader != null) {
                String jsessionid = cookieHeader.split(";")[0];
                cachedJsessionId = jsessionid;
                cachedLoginParams = params;
                conn.disconnect();
                return jsessionid;
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 编码URL中的参数值
     */
    public static String encodeParameterValues(String url) throws UnsupportedEncodingException {
        if (url == null || url.isEmpty()) {
            return url;
        }

        String[] parts = url.split("\\?", 2);
        if (parts.length < 2) {
            return url;
        }

        String baseUrl = parts[0];
        String queryString = parts[1];

        Map<String, String> params = parseQueryString(queryString);

        StringBuilder encodedQuery = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (encodedQuery.length() > 0) {
                encodedQuery.append("&");
            }
            encodedQuery.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return baseUrl + "?" + encodedQuery.toString();
    }

    private static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new LinkedHashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : "";
            params.put(key, value);
        }

        return params;
    }
}
