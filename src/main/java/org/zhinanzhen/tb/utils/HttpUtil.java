package org.zhinanzhen.tb.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求工具类
 * 提供统一的 REST API 调用方法
 */
public class HttpUtil {
    
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 默认超时时间（毫秒）
    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    private static final int DEFAULT_READ_TIMEOUT = 30000;
    
    static {
        // 配置超时
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        factory.setReadTimeout(DEFAULT_READ_TIMEOUT);
        ((RestTemplate) restTemplate).setRequestFactory(factory);
    }
    
    /**
     * 发送 GET 请求
     * @param url 请求地址
     * @return 响应字符串
     */
    public static String get(String url) {
        return execute(url, HttpMethod.GET, null, null, String.class);
    }
    
    /**
     * 发送 GET 请求（带请求头）
     * @param url 请求地址
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String get(String url, Map<String, String> headers) {
        return execute(url, HttpMethod.GET, headers, null, String.class);
    }
    
    /**
     * 发送 GET 请求，返回 JsonNode
     * @param url 请求地址
     * @return JsonNode 对象
     */
    public static JsonNode getForJson(String url) {
        String response = get(url);
        try {
            return objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("解析 JSON 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送 POST 请求（JSON 格式）
     * @param url 请求地址
     * @param requestBody 请求体（Map 格式）
     * @return 响应字符串
     */
    public static String postJson(String url, Map<String, Object> requestBody) {
        return postJson(url, null, requestBody);
    }
    
    /**
     * 发送 POST 请求（JSON 格式）
     * @param url 请求地址
     * @param headers 请求头
     * @param requestBody 请求体（Map 格式）
     * @return 响应字符串
     */
    public static String postJson(String url, Map<String, String> headers, Map<String, Object> requestBody) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            return post(url, headers, jsonBody, MediaType.APPLICATION_JSON);
        } catch (Exception e) {
            throw new RuntimeException("转换 JSON 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送 POST 请求（JSON 格式，直接传 JSON 字符串）
     * @param url 请求地址
     * @param jsonBody JSON 字符串
     * @return 响应字符串
     */
    public static String postJson(String url, String jsonBody) {
        return post(url, null, jsonBody, MediaType.APPLICATION_JSON);
    }
    
    /**
     * 发送 POST 请求（表单格式）
     * @param url 请求地址
     * @param formData 表单数据
     * @return 响应字符串
     */
    public static String postForm(String url, Map<String, String> formData) {
        return postForm(url, null, formData);
    }
    
    /**
     * 发送 POST 请求（表单格式）
     * @param url 请求地址
     * @param headers 请求头
     * @param formData 表单数据
     * @return 响应字符串
     */
    public static String postForm(String url, Map<String, String> headers, Map<String, String> formData) {
        // 构建表单数据
        StringBuilder formBody = new StringBuilder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (formBody.length() > 0) {
                formBody.append("&");
            }
            formBody.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return post(url, headers, formBody.toString(), MediaType.APPLICATION_FORM_URLENCODED);
    }
    
    /**
     * 通用的 POST 请求方法
     * @param url 请求地址
     * @param headers 请求头
     * @param body 请求体
     * @param contentType Content-Type
     * @return 响应字符串
     */
    private static String post(String url, Map<String, String> headers, String body, MediaType contentType) {
        return execute(url, HttpMethod.POST, headers, body, String.class, contentType);
    }
    
    /**
     * 发送 PUT 请求（JSON 格式）
     * @param url 请求地址
     * @param requestBody 请求体
     * @return 响应字符串
     */
    public static String putJson(String url, Map<String, Object> requestBody) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            return put(url, null, jsonBody, MediaType.APPLICATION_JSON);
        } catch (Exception e) {
            throw new RuntimeException("转换 JSON 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 通用的 PUT 请求方法
     */
    private static String put(String url, Map<String, String> headers, String body, MediaType contentType) {
        return execute(url, HttpMethod.PUT, headers, body, String.class, contentType);
    }
    
    /**
     * 发送 DELETE 请求
     * @param url 请求地址
     * @return 响应字符串
     */
    public static String delete(String url) {
        return execute(url, HttpMethod.DELETE, null, null, String.class);
    }
    
    /**
     * 发送 DELETE 请求（带请求头）
     * @param url 请求地址
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String delete(String url, Map<String, String> headers) {
        return execute(url, HttpMethod.DELETE, headers, null, String.class);
    }
    
    /**
     * 核心执行方法
     */
    private static <T> T execute(String url, HttpMethod method, Map<String, String> headers, 
                                   Object body, Class<T> responseType) {
        return execute(url, method, headers, body, responseType, MediaType.APPLICATION_JSON);
    }
    
    /**
     * 核心执行方法（带 Content-Type）
     */
    private static <T> T execute(String url, HttpMethod method, Map<String, String> headers, 
                                   Object body, Class<T> responseType, MediaType contentType) {
        try {
            // 构建请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpHeaders.set(entry.getKey(), entry.getValue());
                }
            }
            
            // 设置默认 Content-Type
            if (body != null && !httpHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
                httpHeaders.setContentType(contentType);
            }
            
            // 创建请求实体
            HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);
            
            // 发送请求
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            
            // 检查响应状态
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("请求失败，状态码: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            // 4xx 错误
            throw new RuntimeException("客户端错误 (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            // 5xx 错误
            throw new RuntimeException("服务端错误 (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            // 其他网络错误
            throw new RuntimeException("网络请求异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建通用请求头
     */
    public static Map<String, String> buildAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("Accept", "application/json");
        return headers;
    }
    
    /**
     * 构建通用请求头（带自定义 Header）
     */
    public static Map<String, String> buildHeaders(Map<String, String> customHeaders) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        if (customHeaders != null) {
            headers.putAll(customHeaders);
        }
        return headers;
    }
}