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

    // GET请求发送
    public static JSONObject sendGet(String url) {
        JSONObject jsonss = null;
        BufferedReader in = null;
        try {
            //String urlNameString = url + "?" + param;
            url = encodeParameterValues(url);
            URL realUrl = new URL(url);
//            System.out.println("请求路径为：" + url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            //connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();

            // 获取所有响应头字段
            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8") // 明确指定字符集
            );

            String line;

            StringBuilder result = new StringBuilder(); // 建议使用 StringBuilder 拼接字符串
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

            // 解析完整的 JSON 字符串
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
    public static JSONObject sendPost(String url,String params, StringBuilder formData) {
        JSONObject jsonss = null;
        try {
            String strRead = null;
            String jsessionid = "";
            // 登录并保存JSESSIONID
            URL urlT = new URL("http://127.0.0.1:8001/admin_v2.1/admin_user/login");
            HttpURLConnection conn = (HttpURLConnection) urlT.openConnection();

            // 设置POST请求
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // 发送登录数据（示例）
//            String params = "username=2&password=sulei123&captcha=znz24qwe";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes());
            }

            // 从响应头提取JSESSIONID
            String cookieHeader = conn.getHeaderField("Set-Cookie");
            if (cookieHeader != null) {
                // 简化提取：实际应解析所有Cookie
                jsessionid = cookieHeader.split(";")[0];
//                System.out.println("获取到JSESSIONID: " + jsessionid);
            }
            conn.disconnect();


            // 移除URL末尾的问号，除非你有实际参数
            URL realUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // 设置请求头（明确指定UTF-8编码）
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cookie", jsessionid);

            // 发送请求数据（使用UTF-8编码）
            try (OutputStream os = connection.getOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                writer.write(formData.toString());
                writer.flush();
            }

            // 检查响应代码
            int responseCode = connection.getResponseCode();
//            System.out.println("Response Code: " + responseCode);

            // 读取响应
            try (InputStream is = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                while ((strRead = reader.readLine()) != null) {
                    jsonss = JSONObject.parseObject(strRead);
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonss;
    }


    /**
     * 编码URL中的参数值
     * @param url 原始URL
     * @return 参数值编码后的URL
     * @throws UnsupportedEncodingException 如果编码失败
     */
    public static String encodeParameterValues(String url) throws UnsupportedEncodingException {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // 分割URL和参数部分
        String[] parts = url.split("\\?", 2);
        if (parts.length < 2) {
            return url; // 没有参数部分，直接返回
        }

        String baseUrl = parts[0];
        String queryString = parts[1];

        // 解析参数
        Map<String, String> params = parseQueryString(queryString);

        // 编码参数值并重新构建查询字符串
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

    /**
     * 解析查询字符串为键值对
     * @param queryString 查询字符串
     * @return 参数键值对
     */
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
