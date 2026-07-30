package org.zhinanzhen.tb.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.pds20220301.AsyncClient;
import com.aliyun.sdk.service.pds20220301.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class WangPanUtils {

    @Value("${aliyun.ACCESSKEYID}")
    private String ACCESS_KEY_ID;

    @Value("${aliyun.ACCESSKEYSECRET}")
    private String ACCESS_KEY_SECRET;

    @Value("${aliyun.PDSENDPOINT}")
    private String PDS_ENDPOINT;

    public JsonNode listFile(String driveId, String parentFileId) throws IOException, ExecutionException, InterruptedException {
        try (AsyncClient asyncClient = getAsyncClient()) {
            ListFileRequest listFileRequest = ListFileRequest.builder()
                    .driveId(driveId)
                    .parentFileId(parentFileId)
                    .limit(100)
                    .build();
            CompletableFuture<ListFileResponse> listFileResponseCompletableFuture = asyncClient.listFile(listFileRequest);
            ListFileResponse listFileResponse = listFileResponseCompletableFuture.get();
            String json = new Gson().toJson(listFileResponse);
            return new ObjectMapper().readTree(json);
        }
    }

    public JsonNode getFile(String driveId, String fileId) throws ExecutionException, InterruptedException, IOException {
        try (AsyncClient asyncClient = getAsyncClient()) {
            // Parameter settings for API request
            GetFileRequest getFileRequest = GetFileRequest.builder()
                    .fileId(fileId)
                    .driveId(driveId)
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();

            // Asynchronously get the return value of the API request
            CompletableFuture<GetFileResponse> response = asyncClient.getFile(getFileRequest);
            // Synchronously get the return value of the API request
            GetFileResponse resp = response.get();
            String json = new Gson().toJson(resp);
            return new ObjectMapper().readTree(json);
        }
    }

    public JsonNode copyFile(String oldDriverId, String newDriverId, String fileId, String newParentFiledId) throws IOException, ExecutionException, InterruptedException {
        try (AsyncClient asyncClient = getAsyncClient()) {
            // Parameter settings for API request
            CopyFileRequest copyFileRequest = CopyFileRequest.builder()
                    .driveId(oldDriverId)
                    .fileId(fileId)
                    .toDriveId(newDriverId)
                    .toParentFileId(newParentFiledId)
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();
            CompletableFuture<CopyFileResponse> resp = asyncClient.copyFile(copyFileRequest);
            CopyFileResponse copyFileResponse = resp.get();
            String json = new Gson().toJson(copyFileResponse);
            return new ObjectMapper().readTree(json);
        }
    }

    public JsonNode getFileByName(String driveId, String fileName) throws ExecutionException, InterruptedException, IOException {
        try (AsyncClient asyncClient = getAsyncClient()) {
            String name = "name=\"" + fileName + "\"";
            SearchFileRequest build = SearchFileRequest.builder()
                    .driveId(driveId)
                    .query(name)
                    .build();
            CompletableFuture<SearchFileResponse> file = asyncClient.searchFile(build);
            SearchFileResponse searchFileResponse = file.get();
            String json = new Gson().toJson(searchFileResponse);
            return new ObjectMapper().readTree(json);
        }
    }

    public CloudDiskFile buildCloudDiskFile(JsonNode item) {
        long fileSize = 0L;
        String type = item.get("type").asText();
        if (!"folder".equalsIgnoreCase(type)) {
            fileSize = item.get("size").asLong();
        }
        return CloudDiskFile.builder()
                .fileId(item.get("fileId").asText())
                .driveId(item.get("driveId").asText())
                .type(type)
                .domainId(item.get("domainId").asText())
                .parentFileId(item.get("parentFileId").asText())
                .name(item.get("name").asText())
                .fileSize(fileSize).build();

    }

    private AsyncClient getAsyncClient() {
        // Configure Credentials authentication information, including ak, secret, token
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                // Please ensure that the environment variables ALIBABA_CLOUD_ACCESS_KEY_ID and ALIBABA_CLOUD_ACCESS_KEY_SECRET are set.
                .accessKeyId(ACCESS_KEY_ID)
                .accessKeySecret(ACCESS_KEY_SECRET)
                //.securityToken(System.getenv("ALIBABA_CLOUD_SECURITY_TOKEN")) // use STS token
                .build());

        // Configure the Client
        AsyncClient client = AsyncClient.builder()
                .region("cn-beijing") // Region ID
                //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
                .credentialsProvider(provider)
                //.serviceConfiguration(Configuration.create()) // Service-level configuration
                // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                // Endpoint 请参考 https://api.aliyun.com/product/pds
                                .setEndpointOverride(PDS_ENDPOINT)
                        //.setConnectTimeout(Duration.ofSeconds(30))
                )
                .build();
        return client;
    }
}
