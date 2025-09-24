package org.zhinanzhen.b.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.pds20220301.AsyncClient;
import com.aliyun.sdk.service.pds20220301.models.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.ikasoa.core.utils.ObjectUtil;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.config.GlobalThreadPool;
import org.zhinanzhen.b.dao.CloudDiskFileDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.UserCloud;
import org.zhinanzhen.b.dao.pojo.UserInfo;
import org.zhinanzhen.b.service.CloudDiskService;
import org.zhinanzhen.b.service.UploadResponseData;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.b.service.pojo.PartInfo;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.utils.PatternMatcherUtil;
import org.zhinanzhen.tb.utils.SendEmailUtil;
import org.zhinanzhen.tb.utils.WangPanUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CloudDiskServiceImpl implements CloudDiskService  {
    @Value("${aliyun.ACCESSKEYID}")
    private String ACCESS_KEY_ID;

    @Value("${aliyun.ACCESSKEYSECRET}")
    private String ACCESS_KEY_SECRET;

    @Resource
    private CloudDiskFileDAO cloudDiskFileDAO;

    @Resource
    private UserDAO userDAO;
    @Autowired
    private AdviserDAO adviserDAO;
    @Autowired
    private OfficialDAO officialDAO;
    
    @Autowired
    private WangPanUtils wangPanUtils;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    List<CloudDiskFile> cloudDiskFileList = new ArrayList<>();
    @Autowired
    private AdminUserDAO adminUserDAO;

    @Override
    public int addAndUpdate(MultipartFile file, String type, Integer applicantId,Integer userId, String parentFileId,
                            Integer adviserId, Integer id, String folderName, Integer officialId, String relativePath) {
        if ("file".equalsIgnoreCase(type) && file == null) {
            throw new RuntimeException("上传文件为空");
        }
        if ("folder".equalsIgnoreCase(type)) {
            try {
                CloudDiskFile cloudDiskFile = new CloudDiskFile();
                // 创建上传文件的请求并获取上传链接
                // Configure Credentials authentication information, including ak, secret, token
                AsyncClient client = getAsyncClient();

                CreateFileRequest createFileRequest = null;
                if (id == null) {
                    createFileRequest = CreateFileRequest.builder()
                            .name(folderName)
                            .type(type)
                            .parentFileId(parentFileId)
                            .driveId("1020")
                            .build();
                }
                cloudDiskFile = cloudDiskFileDAO.getById(id, parentFileId, null, folderName, null);
                if (cloudDiskFile != null && cloudDiskFile.getName().equals(folderName)) {
                    return -1;
                }
                // Asynchronously get the return value of the API request
                CompletableFuture<CreateFileResponse> response = client.createFile(createFileRequest);
                // Synchronously get the return value of the API request
                CreateFileResponse resp = response.get();
                System.out.println(new Gson().toJson(resp));
                // Asynchronous processing of return values
                String json = new Gson().toJson(resp);
                JSONObject jsonObject = JSON.parseObject(json);
                JSONObject body1 = jsonObject.getJSONObject("body");
                String fileId = body1.get("fileId").toString();
                client.close();

                if (id == null) {
                    cloudDiskFile = CloudDiskFile.builder().fileId(fileId).parentFileId(parentFileId).
                            domainId("bj21743").name(folderName).type(type).driveId("1020").userId(userId).
                            applicantId(applicantId).adviserId(adviserId).officialId(officialId).build();
                    if ("root".equalsIgnoreCase(parentFileId)) {
                        cloudDiskFile.setRelativePath("/root" + "/" + folderName);
                    } else {
                        CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, parentFileId, null, null);
                        cloudDiskFile.setRelativePath(byId.getRelativePath() + "/" + folderName);
                    }
                    if (adviserId != null) {
                        AdviserDO adviserById = adviserDAO.getAdviserById(adviserId);
                        cloudDiskFile.setOperator(adviserById.getName());
                    }
                    if (officialId != null) {
                        OfficialDO officialById = officialDAO.getOfficialById(officialId);
                        cloudDiskFile.setOperator(officialById.getName());
                    }
                    int add = cloudDiskFileDAO.add(cloudDiskFile);
                    if (add > 0) {
                        return add;
                    }
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(type)) {
            String fileName = file.getOriginalFilename().replace(" ", "_").replace("%20", "_");// 文件原名称
            Long fileSize = 0L;
            fileSize = file.getSize();
            log.info("上传的文件原名称:" + fileName);
            // 判断文件类型
            String fileType = fileName.indexOf(".") != -1
                    ? fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
                    : null;
            if (ObjectUtil.isNull(fileType))
                throw new RuntimeException("文件类型为空");

            try {
                CloudDiskFile cloudDiskFile = new CloudDiskFile();
                // 创建上传文件的请求并获取上传链接
                // Configure Credentials authentication information, including ak, secret, token
                AsyncClient client = getAsyncClient();

                File fileTmp = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
                file.transferTo(fileTmp);
                // Parameter settings for API request
                CreateFileRequest.ParallelSha1Ctx partInfoList0ParallelSha1Ctx = CreateFileRequest.ParallelSha1Ctx.builder()
                        .partOffset(fileTmp.length())
                        .build();
                CreateFileRequest.PartInfoList partInfoList0 = CreateFileRequest.PartInfoList.builder()
                        .partNumber(1)
                        .parallelSha1Ctx(partInfoList0ParallelSha1Ctx)
                        .build();

                CreateFileRequest createFileRequest = null;
                if (id == null) {
                    createFileRequest = CreateFileRequest.builder()
                            .name(fileName)
                            .type(type)
                            .parentFileId(parentFileId)
                            .driveId("1020")
                            .size(fileTmp.length())
                            .partInfoList(java.util.Arrays.asList(
                                    partInfoList0
                            ))
                            .build();
                }
                cloudDiskFile = cloudDiskFileDAO.getById(id, parentFileId, null, fileName, null);
                if (cloudDiskFile != null && cloudDiskFile.getName().equals(fileName)) {
                    return -1;
//                    createFileRequest = CreateFileRequest.builder()
//                            .name(fileName)
//                            .type(type)
//                            .parentFileId(parentFileId)
//                            .driveId("101")
//                            .size(fileTmp.length())
//                            .fileId(cloudDiskFile.getFileId())
//                            .partInfoList(java.util.Arrays.asList(
//                                    partInfoList0
//                            ))
//                            .build();
                }
                // Asynchronously get the return value of the API request
                CompletableFuture<CreateFileResponse> response = client.createFile(createFileRequest);
                // Synchronously get the return value of the API request
                CreateFileResponse resp = response.get();
                System.out.println(new Gson().toJson(resp));
                // Asynchronous processing of return values
                String json = new Gson().toJson(resp);
                JSONObject jsonObject = JSON.parseObject(json);
                JSONObject body1 = jsonObject.getJSONObject("body");

                // 文件进行上传
                String jsonString = JSONObject.toJSONString(body1);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                UploadResponseData responseData = objectMapper.readValue(jsonString, UploadResponseData.class);
                List<PartInfo> partInfoList = responseData.getPartInfoList();
                String fileId = body1.get("fileId").toString();
                System.out.println(fileId);
                String uploadId = body1.get("uploadId").toString();

                // 遍历所有分片
                for (PartInfo uploadPartInfo : partInfoList) {

                    // 计算分片在本地文件中的位置
                    int number = uploadPartInfo.getPartNumber();
                    long pos = (number - 1) * fileTmp.length();
                    //            long size = Math.min(length - pos, file.length());
                    long size = fileTmp.length();
                    byte[] partContent = new byte[(int) size];

                    // 从本地文件中读取分片内容到内存中
                    RandomAccessFile randomAccessFile = new RandomAccessFile(fileTmp, "r");
                    randomAccessFile.seek(pos);
                    randomAccessFile.readFully(partContent, 0, (int) size);
                    randomAccessFile.close();

                    // 上传分片
                    RequestBody body = RequestBody.create(null, partContent);
                    Request request = new Request.Builder()
                            .url(uploadPartInfo.getUploadUrl())
                            .header("Content-Length", String.valueOf(size))
                            .put(body)
                            .build();

                    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                    okhttp3.Response response1 = okHttpClient.newCall(request).execute();

                    // 判断分片是否上传成功
                    if (!response1.isSuccessful()) {
                        System.out.println(response1.body().string() + "\n");
                        Assert.fail("upload part failed, partNumber:" + number);
                    }
                    System.out.println("upload part success, partNumber:" + number);
                }
                // 完成文件上传

                CompleteFileRequest completeFileRequest = CompleteFileRequest.builder()
                        .driveId("1020")
                        .fileId(fileId)
                        .uploadId(uploadId)
                        // Request-level configuration rewrite, can set Http request parameters, etc.
                        // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                        .build();

                CompletableFuture<CompleteFileResponse> responseT = client.completeFile(completeFileRequest);
                CompleteFileResponse respt = responseT.get();
                String json1 = new Gson().toJson(respt);
                JSONObject jsonObject1 = JSON.parseObject(json1);
                JSONObject body = jsonObject1.getJSONObject("body");
                String fileIdTmp = body.get("fileId").toString();
                String parentFileIdTmp = body.get("parentFileId").toString();
                String driveId = body.get("driveId").toString();
                // Finally, close the client
                client.close();

                if (id == null) {
                    cloudDiskFile = CloudDiskFile.builder().fileId(fileIdTmp).parentFileId(parentFileIdTmp).
                            domainId("bj21743").name(fileName).type(type).driveId(driveId).applicantId(applicantId)
                            .userId(userId).adviserId(adviserId).officialId(officialId).fileSize(fileSize).build();
                    if ("root".equalsIgnoreCase(parentFileId)) {
                        cloudDiskFile.setRelativePath("/root" + "/" + folderName);
                    } else {
                        CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, parentFileId, null, null);
                        cloudDiskFile.setRelativePath(byId.getRelativePath() + "/" + fileName);
                    }
                    if (adviserId != null) {
                        AdviserDO adviserById = adviserDAO.getAdviserById(adviserId);
                        cloudDiskFile.setOperator(adviserById.getName());
                    }
                    if (officialId != null) {
                        OfficialDO officialById = officialDAO.getOfficialById(officialId);
                        cloudDiskFile.setOperator(officialById.getName());
                    }
                    String downloadUrl = getDownloadUrl(cloudDiskFile.getFileId());
                    cloudDiskFile.setDownloadUrl(downloadUrl);
                    int add = cloudDiskFileDAO.add(cloudDiskFile);
                    if (add > 0) {
                        return add;
                    }
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (JsonParseException e) {
                throw new RuntimeException(e);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return -1;
    }

    @Override
    public int addAndUpdate(MultipartFile file, Integer userId, String parentFileId, Integer adviserId, Integer officialId, String relativePath) {
        String[] split = relativePath.split("/");
        for (int i = 0; i < split.length; i++) {
            if (i < split.length - 1) {
                try {
                    // 使用组合键作为锁对象
                    String lockKey = (parentFileId + ":" + split[i]).intern();
                    synchronized (lockKey) {
                        CloudDiskFile cloudDiskFile = new CloudDiskFile();
                        // 创建上传文件的请求并获取上传链接
                        // Configure Credentials authentication information, including ak, secret, token
                        AsyncClient client = getAsyncClient();

                        CreateFileRequest createFileRequest = null;
                        createFileRequest = CreateFileRequest.builder()
                                .name(split[i])
                                .type("folder")
                                .parentFileId(parentFileId)
                                .driveId("1020")
                                .build();
                        cloudDiskFile = cloudDiskFileDAO.getById(null, parentFileId, null, split[i], null);
                        if (cloudDiskFile != null && cloudDiskFile.getName().equals(split[i])) {
                            parentFileId = cloudDiskFile.getFileId();
                            continue;
                        }
                        // Asynchronously get the return value of the API request
                        CompletableFuture<CreateFileResponse> response = client.createFile(createFileRequest);
                        // Synchronously get the return value of the API request
                        CreateFileResponse resp = response.get();
                        System.out.println(new Gson().toJson(resp));
                        // Asynchronous processing of return values
                        String json = new Gson().toJson(resp);
                        JSONObject jsonObject = JSON.parseObject(json);
                        JSONObject body1 = jsonObject.getJSONObject("body");
                        String fileId = body1.get("fileId").toString();
                        client.close();

                        cloudDiskFile = CloudDiskFile.builder().fileId(fileId).parentFileId(parentFileId).
                                domainId("bj21743").name(split[i]).type("folder").driveId("1020").userId(userId)
                                .adviserId(adviserId).officialId(officialId).build();
                        if ("root".equalsIgnoreCase(parentFileId)) {
                            cloudDiskFile.setRelativePath("/root" + "/" + split[i]);
                        } else {
                            CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, parentFileId, null, null);
                            cloudDiskFile.setRelativePath(byId.getRelativePath() + "/" + split[i]);
                        }
                        if (adviserId != null) {
                            AdviserDO adviserById = adviserDAO.getAdviserById(adviserId);
                            cloudDiskFile.setOperator(adviserById.getName());
                        }
                        if (officialId != null) {
                            OfficialDO officialById = officialDAO.getOfficialById(officialId);
                            cloudDiskFile.setOperator(officialById.getName());
                        }
                        int add = cloudDiskFileDAO.add(cloudDiskFile);
                        parentFileId = fileId;
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else {
                String lockKey = (parentFileId + ":" + split[i]).intern();
                synchronized (lockKey) {
                    String fileName = file.getOriginalFilename().replace(" ", "_").replace("%20", "_");// 文件原名称
                    Long fileSize = 0L;
                    fileSize = file.getSize();
                    log.info("上传的文件原名称:" + fileName);
                    // 判断文件类型
                    String fileType = fileName.indexOf(".") != -1
                            ? fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
                            : null;
                    if (ObjectUtil.isNull(fileType))
                        throw new RuntimeException("文件类型为空");

                    try {
                        CloudDiskFile cloudDiskFile = new CloudDiskFile();
                        cloudDiskFile = cloudDiskFileDAO.getById(null, parentFileId, null, fileName, null);
                        if (cloudDiskFile != null && cloudDiskFile.getName().equals(fileName)) {
                            return -1;
                        }
                        // 创建上传文件的请求并获取上传链接
                        // Configure Credentials authentication information, including ak, secret, token
                        AsyncClient client = getAsyncClient();

                        File fileTmp = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
                        file.transferTo(fileTmp);
                        // Parameter settings for API request
                        CreateFileRequest.ParallelSha1Ctx partInfoList0ParallelSha1Ctx = CreateFileRequest.ParallelSha1Ctx.builder()
                                .partOffset(fileTmp.length())
                                .build();
                        CreateFileRequest.PartInfoList partInfoList0 = CreateFileRequest.PartInfoList.builder()
                                .partNumber(1)
                                .parallelSha1Ctx(partInfoList0ParallelSha1Ctx)
                                .build();

                        CreateFileRequest createFileRequest = null;
                        createFileRequest = CreateFileRequest.builder()
                                .name(fileName)
                                .type("file")
                                .parentFileId(parentFileId)
                                .driveId("1020")
                                .size(fileTmp.length())
                                .partInfoList(java.util.Arrays.asList(
                                        partInfoList0
                                ))
                                .build();
                        cloudDiskFile = cloudDiskFileDAO.getById(null, parentFileId, null, fileName, null);
                        if (cloudDiskFile != null && cloudDiskFile.getName().equals(fileName)) {
                            return -1;
//                    createFileRequest = CreateFileRequest.builder()
//                            .name(fileName)
//                            .type(type)
//                            .parentFileId(parentFileId)
//                            .driveId("101")
//                            .size(fileTmp.length())
//                            .fileId(cloudDiskFile.getFileId())
//                            .partInfoList(java.util.Arrays.asList(
//                                    partInfoList0
//                            ))
//                            .build();
                        }
                        // Asynchronously get the return value of the API request
                        CompletableFuture<CreateFileResponse> response = client.createFile(createFileRequest);
                        // Synchronously get the return value of the API request
                        CreateFileResponse resp = response.get();
                        System.out.println(new Gson().toJson(resp));
                        // Asynchronous processing of return values
                        String json = new Gson().toJson(resp);
                        JSONObject jsonObject = JSON.parseObject(json);
                        JSONObject body1 = jsonObject.getJSONObject("body");

                        // 文件进行上传
                        String jsonString = JSONObject.toJSONString(body1);
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        UploadResponseData responseData = objectMapper.readValue(jsonString, UploadResponseData.class);
                        List<PartInfo> partInfoList = responseData.getPartInfoList();
                        String fileId = body1.get("fileId").toString();
                        System.out.println(fileId);
                        String uploadId = body1.get("uploadId").toString();

                        // 遍历所有分片
                        for (PartInfo uploadPartInfo : partInfoList) {

                            // 计算分片在本地文件中的位置
                            int number = uploadPartInfo.getPartNumber();
                            long pos = (number - 1) * fileTmp.length();
                            //            long size = Math.min(length - pos, file.length());
                            long size = fileTmp.length();
                            byte[] partContent = new byte[(int) size];

                            // 从本地文件中读取分片内容到内存中
                            RandomAccessFile randomAccessFile = new RandomAccessFile(fileTmp, "r");
                            randomAccessFile.seek(pos);
                            randomAccessFile.readFully(partContent, 0, (int) size);
                            randomAccessFile.close();

                            // 上传分片
                            RequestBody body = RequestBody.create(null, partContent);
                            Request request = new Request.Builder()
                                    .url(uploadPartInfo.getUploadUrl())
                                    .header("Content-Length", String.valueOf(size))
                                    .put(body)
                                    .build();

                            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                            okhttp3.Response response1 = okHttpClient.newCall(request).execute();

                            // 判断分片是否上传成功
                            if (!response1.isSuccessful()) {
                                System.out.println(response1.body().string() + "\n");
                                Assert.fail("upload part failed, partNumber:" + number);
                            }
                            System.out.println("upload part success, partNumber:" + number);
                        }
                        // 完成文件上传

                        CompleteFileRequest completeFileRequest = CompleteFileRequest.builder()
                                .driveId("1020")
                                .fileId(fileId)
                                .uploadId(uploadId)
                                // Request-level configuration rewrite, can set Http request parameters, etc.
                                // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                                .build();

                        CompletableFuture<CompleteFileResponse> responseT = client.completeFile(completeFileRequest);
                        CompleteFileResponse respt = responseT.get();
                        String json1 = new Gson().toJson(respt);
                        JSONObject jsonObject1 = JSON.parseObject(json1);
                        JSONObject body = jsonObject1.getJSONObject("body");
                        String fileIdTmp = body.get("fileId").toString();
                        String parentFileIdTmp = body.get("parentFileId").toString();
                        String driveId = body.get("driveId").toString();
                        // Finally, close the client
                        client.close();

                        cloudDiskFile = CloudDiskFile.builder().fileId(fileIdTmp).parentFileId(parentFileIdTmp).
                                domainId("bj21743").name(fileName).type("file").driveId(driveId).userId(userId).adviserId(adviserId).officialId(officialId).fileSize(fileSize).build();
                        if ("root".equalsIgnoreCase(parentFileId)) {
                            cloudDiskFile.setRelativePath("/root" + "/" + fileName);
                        } else {
                            CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, parentFileId, null, null);
                            cloudDiskFile.setRelativePath(byId.getRelativePath() + "/" + fileName);
                        }
                        if (adviserId != null) {
                            AdviserDO adviserById = adviserDAO.getAdviserById(adviserId);
                            cloudDiskFile.setOperator(adviserById.getName());
                        }
                        if (officialId != null) {
                            OfficialDO officialById = officialDAO.getOfficialById(officialId);
                            cloudDiskFile.setOperator(officialById.getName());
                        }
                        String downloadUrl = getDownloadUrl(cloudDiskFile.getFileId());
                        cloudDiskFile.setDownloadUrl(downloadUrl);
                        int add = cloudDiskFileDAO.add(cloudDiskFile);
                        if (add > 0) {
                            return add;
                        }
                    } catch (ExecutionException | InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (JsonParseException e) {
                        throw new RuntimeException(e);
                    } catch (JsonMappingException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return 1;
    }

    @Override
    public String getDownLink(Integer id, String fileId) {
        String downloadUrl = "";
        CloudDiskFile cloudDiskFile = cloudDiskFileDAO.getById(id, null, fileId, null, null);
        if (ObjectUtil.isNull(cloudDiskFile) || !fileId.equalsIgnoreCase(cloudDiskFile.getFileId())) {
            throw new RuntimeException("文件信息错误或不存在");
        }
        if ("file".equalsIgnoreCase(cloudDiskFile.getType())) {
            downloadUrl = getDownloadUrl(cloudDiskFile.getFileId());
        }
        if ("folder".equalsIgnoreCase(cloudDiskFile.getType())) {
            try {
                AsyncClient client = getAsyncClient();
                // Parameter settings for API request
                CreateShareLinkRequest createShareLinkRequest = CreateShareLinkRequest.builder()
                        .driveId("1020")
                        .shareAllFiles(false)
                        .fileIdList(java.util.Arrays.asList(
                                fileId
                        ))
                        .userId("1cc43dd77f0e4cdb9e382890e52e954c")
                        .officeEditable(true)
                        // Request-level configuration rewrite, can set Http request parameters, etc.
                        // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                        .build();
                // Asynchronously get the return value of the API request
                CompletableFuture<CreateShareLinkResponse> response = client.createShareLink(createShareLinkRequest);
                // Synchronously get the return value of the API request
                CreateShareLinkResponse resp = response.get();
                String json = new Gson().toJson(resp);
                JSONObject jsonObject = JSON.parseObject(json);
                JSONObject body = jsonObject.getJSONObject("body");
                String shareId = body.get("shareId").toString();
                downloadUrl = "https://bj21743.apps.aliyunfile.com/disk/s/" + shareId;
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return downloadUrl;
    }

    @Override
    public int updateofficialId(Integer officialId1, Integer integer) {
        int update = cloudDiskFileDAO.updateofficialId(officialId1, integer);
        return 0;
    }

    @Override
    public List<UserCloud> listUserCloud(String userName, String email, int pageNum, int pageSize) {
        return cloudDiskFileDAO.listUserCloud(userName, email, pageNum * pageSize, pageSize);
    }

    @Override
    public void synchronizeUserCloud() {
        try {
            AsyncClient client = getAsyncClient();
            // Parameter settings for API request
            ListUserRequest listUserRequest = ListUserRequest.builder()
                    .limit(999)
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();
            // Asynchronously get the return value of the API request
            CompletableFuture<ListUserResponse> response = client.listUser(listUserRequest);
            // Synchronously get the return value of the API request
            ListUserResponse resp = response.get(30, TimeUnit.SECONDS);
            String json = new Gson().toJson(resp);
            JSONObject jsonObject = JSON.parseObject(json);
            JSONObject body1 = jsonObject.getJSONObject("body");
            String jsonString = JSONObject.toJSONString(body1.get("items"));
            List<UserInfo> userInfos = JSONArray.parseArray(jsonString, UserInfo.class);
            ThreadPoolExecutor executor = GlobalThreadPool.getInstance();
            CountDownLatch latch = new CountDownLatch(userInfos.size());
            for (UserInfo userInfo : userInfos) {
                executor.submit(() -> {
                    try {
                        UserCloud userCloud = new UserCloud();
                        userCloud.setUserName(userInfo.getUserName());
                        userCloud.setEmail(userInfo.getUserName());
                        AdviserDO adviserDO = adviserDAO.getAdviserByEmail(userInfo.getUserName());
                        if (adviserDO != null) {
                            userCloud.setAdviserId(adviserDO.getId());
                        }
                        OfficialDO officialDO = officialDAO.getOfficialByEmail(userInfo.getUserName());
                        if (officialDO != null) {
                            userCloud.setOfficialId(officialDO.getId());
                        }
                        String userId = userInfo.getUserId();
                        userCloud.setUserId(userId);
                        // Parameter settings for API request
                        SearchDriveRequest searchDriveRequest = SearchDriveRequest.builder()
                                .owner(userId)
                                // Request-level configuration rewrite, can set Http request parameters, etc.
                                // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                                .build();
                        // Asynchronously get the return value of the API request
                        CompletableFuture<SearchDriveResponse> responseT = client.searchDrive(searchDriveRequest);
                        // Synchronously get the return value of the API request
                        SearchDriveResponse respT = responseT.get();
                        String jsonT = new Gson().toJson(respT);
                        JSONObject jsonObjectT = JSON.parseObject(jsonT);
                        JSONObject body1T = jsonObjectT.getJSONObject("body");
                        String string = body1T.get("items").toString();
                        // 解析JSON数组
                        JsonNode rootNode = objectMapper.readTree(string);
                        // 获取第一个元素的driveId
                        if (rootNode.isArray() && rootNode.size() > 0) {
                            JsonNode firstElement = rootNode.get(0);
                            String driveId = firstElement.get("driveId").asText();
                            userCloud.setDriveId(driveId);
                        }
                        List<UserCloud> userCloudList = cloudDiskFileDAO.listUserCloudBycondition(userCloud.getDriveId());
                        if (userCloudList.isEmpty()) {
                            latch.countDown();
                            cloudDiskFileDAO.addUserCloud(userCloud);
                        }
                    } catch (InterruptedException | IOException | ExecutionException e) {
                        latch.countDown();
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            AtomicInteger userId = new AtomicInteger();
            List<UserCloud> userClouds = cloudDiskFileDAO.listUserCloud(null, null, 0, 999);
            ThreadPoolExecutor executorT = GlobalThreadPool.getInstance();
            for (UserCloud userCloud : userClouds) {
                executorT.submit(() -> {
                    try {
                        userId.set(userCloud.getId());
                        AsyncClient asyncClient = getAsyncClient();
                        GetUserRequest getUserRequest = GetUserRequest.builder().userId(userCloud.getUserId()).build();
                        CompletableFuture<GetUserResponse> user = asyncClient.getUser(getUserRequest);
                        GetUserResponse getUserResponse = user.get();
                        String jsonT = new Gson().toJson(getUserResponse);
                        JsonNode path = new ObjectMapper().readTree(jsonT).path("body");
                        if (path == null) {
                            cloudDiskFileDAO.deleteUserCloud(userCloud.getId());
                        }
                    } catch (InterruptedException | IOException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        cloudDiskFileDAO.deleteUserCloud(userId.get());
                    }
                });
            }
        }
    }

    @Override
    public int countUserCloud(String userName, String email) {
        return cloudDiskFileDAO.countUserCloud(userName, email);
    }

    @Override
    public void deleteUserCloud(Integer id) {
        try {
            AsyncClient asyncClient = getAsyncClient();
            UserCloud userCloud = cloudDiskFileDAO.getUserCloud(null, null, id, null, null);
            DeleteDriveRequest deleteDriveRequest = DeleteDriveRequest.builder().driveId(userCloud.getDriveId()).build();
            CompletableFuture<DeleteDriveResponse> deleteDriveResponseCompletableFuture = asyncClient.deleteDrive(deleteDriveRequest);
            DeleteDriveResponse deleteDriveResponse = deleteDriveResponseCompletableFuture.get();
            String json = new Gson().toJson(deleteDriveResponse);
            DeleteUserRequest build = DeleteUserRequest.builder()
                    .userId(userCloud.getUserId())
                    .domainId("bj21743")
                    .build();
            CompletableFuture<DeleteUserResponse> deleteUserResponseCompletableFuture = asyncClient.deleteUser(build);
            DeleteUserResponse deleteUserResponse = deleteUserResponseCompletableFuture.get();
            String jsons = new Gson().toJson(deleteUserResponse);
            cloudDiskFileDAO.deleteUserCloud(id);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public UserCloud addUserCloud(String userName, String email, String role, String phone) {
        try {
            UserCloud userCloud1 = cloudDiskFileDAO.getUserCloud(null, null, null, null, phone);
            if (userCloud1 != null) {
                return null;
            }
            String userId = RandomStringUtils.randomAlphanumeric(32);
            AsyncClient asyncClient = getAsyncClient();
            UserCloud userCloud = new UserCloud();
            CreateUserRequest createUserRequest = CreateUserRequest.builder()
                    .nickName(userName)
                    .userId(userId)
                    .role(role)
                    .email(email)
                    .phone(phone)
                    .userName(userName)
                    .status("enabled")
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();
            CompletableFuture<CreateUserResponse> createUserResponseCompletableFuture = asyncClient.createUser(createUserRequest);
            CreateUserResponse createUserResponse = createUserResponseCompletableFuture.get(10, TimeUnit.SECONDS);
            String json = new Gson().toJson(createUserResponse);
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            JsonNode items = jsonNode.path("body");
            String driveId = items.get("defaultDriveId").asText();
            if (!StringUtils.isEmpty(driveId)) {
                userCloud.setDriveId(driveId);
            }
            asyncClient.close();
            asyncClient = getAsyncClient();
            // 修改空间大小
            UpdateDriveRequest updateDriveRequest = UpdateDriveRequest.builder().driveId(userCloud.getDriveId()).owner(userId).totalSize(10737418240L).build();
            CompletableFuture<UpdateDriveResponse> updateDriveResponseCompletableFuture = asyncClient.updateDrive(updateDriveRequest);
            UpdateDriveResponse updateDriveResponse = updateDriveResponseCompletableFuture.get(10, TimeUnit.SECONDS);
            userCloud.setUserId(userId);
            userCloud.setUserName(userName);
            userCloud.setEmail(email);
            userCloud.setPhone(phone);
//            userCloud.setDriveId();
            AdminUserDO adminUserByUsername = adminUserDAO.getAdminUserByUsername(email);
            if (adminUserByUsername != null) {
                userCloud.setAdviserId(adminUserByUsername.getAdviserId());
                userCloud.setOfficialId(adminUserByUsername.getOfficialId());
            }
            cloudDiskFileDAO.addUserCloud(userCloud);
            SendEmailUtil.send("1286559059@qq.com", "添加用户成功", "用户" + userName + "添加成功,请及时修改登录方式");
            asyncClient.close();
            return userCloud;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int deleteById(Integer id, String fileId) {
        List<CloudDiskFile> cloudDiskFileList1 = cloudDiskFileDAO.listByParentFileId(null, fileId, null, null, null, 0, 100);
        if (CollectionUtils.isNotEmpty(cloudDiskFileList1)) {
            return -2;
        }
        CloudDiskFile cloudDiskFile = cloudDiskFileDAO.getById(id, null, fileId, null, null);
        if (ObjectUtil.isNull(cloudDiskFile) || !fileId.equalsIgnoreCase(cloudDiskFile.getFileId())) {
            throw new RuntimeException("文件信息错误或不存在");
        }
        int delete = 0;
        try {
            AsyncClient client = getAsyncClient();
            // Parameter settings for API request
            DeleteFileRequest deleteFileRequest = DeleteFileRequest.builder()
                    .fileId(fileId)
                    .driveId("1020")
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();

            // Asynchronously get the return value of the API request
            CompletableFuture<DeleteFileResponse> response = client.deleteFile(deleteFileRequest);
            // Synchronously get the return value of the API request
            DeleteFileResponse resp = response.get();
            String json = new Gson().toJson(resp);
            JSONObject jsonObject = JSON.parseObject(json);
            client.close();
            cloudDiskFile.setIsDelete(1);
            delete = cloudDiskFileDAO.update(cloudDiskFile);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return delete;
    }

    @Override
    public List<CloudDiskFile> list(Integer id, String parentFileId, String name, Integer applicantId, Integer userId, int pageNum, int pageSize) {
        if (userId != null && StringUtils.isEmpty(parentFileId)) {
            List<CloudDiskFile> cloudDiskFileList1 = cloudDiskFileDAO.listByParentFileId(null, "root", null, null, userId, pageNum* pageSize, pageSize);
            log.info("当前查询用户id----------------------" + userId);
            log.info("当前查询用户资料----------------------" + cloudDiskFileList1);
            if (CollectionUtils.isNotEmpty(cloudDiskFileList1)) {
                String fileId = cloudDiskFileList1.get(0).getFileId();
                List<CloudDiskFile> cloudDiskFileList2 = cloudDiskFileDAO.listByParentFileId(null, fileId, null, null, userId, pageNum * pageSize, pageSize);
                return cloudDiskFileList2;
            } else {
                return null;
            }
        }
        List<CloudDiskFile> cloudDiskFileList = cloudDiskFileDAO.listByParentFileId(id, parentFileId, name, applicantId, userId, pageNum * pageSize, pageSize);
        return cloudDiskFileList;
    }

    @Override
    public int count(Integer id, String parentFileId, String name, Integer applicantId, Integer userId) {
        return cloudDiskFileDAO.count(id, parentFileId, name, applicantId, userId);
    }

    @Override
    public String getShareUrl(String userCode, String parentFileId) {
        if (parentFileId == null) {
            throw new RuntimeException("所选文件夹错误，请核实");
        }
        try {
            AsyncClient client = getAsyncClient();
            // Parameter settings for API request
            CreateShareLinkRequest createShareLinkRequest = CreateShareLinkRequest.builder()
                    .driveId("1020")
                    .shareAllFiles(false)
                    .fileIdList(java.util.Arrays.asList(
                            parentFileId
                    ))
                    .userId(userCode)
                    .officeEditable(true)
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();
            // Asynchronously get the return value of the API request
            CompletableFuture<CreateShareLinkResponse> response = client.createShareLink(createShareLinkRequest);
            // Synchronously get the return value of the API request
            CreateShareLinkResponse resp = response.get();
            String json = new Gson().toJson(resp);
            JSONObject jsonObject = JSON.parseObject(json);
            JSONObject body = jsonObject.getJSONObject("body");
            String shareId = body.get("shareId").toString();
            String shareUrl = "https://bj21743.apps.aliyunfile.com/disk/s/" + shareId;
            CloudDiskFile cloudDiskFile = cloudDiskFileDAO.getById(null, parentFileId, null, null, null);
            cloudDiskFile.setUrl(shareUrl);
            cloudDiskFileDAO.update(cloudDiskFile);
            return shareUrl;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public int getFileStructure(String parentFileStructures, Integer adviserId, Integer officialId, Map<String, String> belongFolderMap, Map<String, Integer> addCountMap, String folderName) {
//        UserCloud userCloud = cloudDiskFileDAO.getUserCloud(adviserId, officialId);
//        if (folderName != null && parentFileStructures == null) {
//            parentFileStructures = getParentFileId(folderName, userCloud.getDriveId());
//        }
//        String driveId = userCloud.getDriveId();
//        if (parentFileStructures == null || parentFileStructures.trim().isEmpty()) {
//            addCountMap.forEach((k, v) -> {
//                List<CloudDiskFile> cloudDiskFileList1 = cloudDiskFileDAO.listByRelativePath("/root/" + k);
//                if (cloudDiskFileList1 != null && cloudDiskFileList1.size() == v) {
//                    return;
//                }
//                for (CloudDiskFile cloudDiskFile : cloudDiskFileList1) {
//                    AsyncClient asyncClient = getAsyncClient();
//                    GetFileRequest build = GetFileRequest.builder()
//                            .driveId(driveId)
//                            .fileId(cloudDiskFile.getFileId())
//                            .build();
//                    CompletableFuture<GetFileResponse> file = asyncClient.getFile(build);
//                    try {
//                        GetFileResponse getFileResponse = file.get();
//                        String json = new Gson().toJson(getFileResponse);
//                        JsonNode jsonNode = new ObjectMapper().readTree(json);
//                        String statusCode = jsonNode.path("statusCode").asText();
//                        if ("200".equalsIgnoreCase(statusCode)) {
//                            continue;
//                        }
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    } catch (ExecutionException e) {
//                        cloudDiskFile.setIsDelete(1);
//                        cloudDiskFileDAO.update(cloudDiskFile);
//                        log.info("文件" + cloudDiskFile.getName() + ":" + cloudDiskFile.getFileId() + "已删除");
//                    } catch (JsonProcessingException e) {
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            });
//            return 0; // 递归终止条件
//        }
//        String[] split = parentFileStructures.split(",");
//        StringBuilder newObjects = new StringBuilder(); // 改用局部变量，避免全局污染
//        if (belongFolderMap!= null && !belongFolderMap.isEmpty()) {
//            AsyncClient client = getAsyncClient();
//            for (String s : split) {
//                try {
//                    String sFildeId = belongFolderMap.get(s);
//                    if (sFildeId == null) {
//                        continue;
//                    }
//                    ListFileRequest build = ListFileRequest.builder()
//                            .driveId(driveId)
//                            .parentFileId(s)
//                            .build();
//                    CompletableFuture<com.aliyun.sdk.service.pds20220301.models.ListFileResponse> listFileResponseCompletableFuture = client.listFile(build);
//                    com.aliyun.sdk.service.pds20220301.models.ListFileResponse listFileResponse = listFileResponseCompletableFuture.get();
//                    String json = new Gson().toJson(listFileResponse);
//                    JsonNode jsonNode = new ObjectMapper().readTree(json);
//                    JsonNode items = jsonNode.path("body").path("items");
//                    for (JsonNode node : items) {
//                        String fileId = node.get("fileId").asText();
//                        if (fileId.equalsIgnoreCase(belongFolderMap.get(s))) {
//                            continue;
//                        }
//                        MoveFileRequest moveFileRequest = MoveFileRequest.builder()
//                                .driveId(driveId)
//                                .fileId(fileId)
//                                .toParentFileId(belongFolderMap.get(s))
//                                .build();
//                        CompletableFuture<MoveFileResponse> moveFileResponseCompletableFuture = client.moveFile(moveFileRequest);
//                        moveFileResponseCompletableFuture.get();
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
////            split = new String[]{parentFileStructures};
//        }
//        belongFolderMap = new HashMap<>();
//        for (String s : split) {
//            try {
//                StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
//                        .accessKeyId(ACCESS_KEY_ID)
//                        .accessKeySecret(ACCESS_KEY_SECRET)
//                        .build());
//
//                MyAsyncClient client = MyAsyncClient.builder()
//                        .region("cn-beijing")
//                        .credentialsProvider(provider)
//                        .overrideConfiguration(
//                                ClientOverrideConfiguration.create()
//                                        .setEndpointOverride("bj21743.api.aliyunpds.com")
//                        )
//                        .build();
//                ListFileRequest listFileRequest = ListFileRequest.builder()
//                        .driveId(driveId)
//                        .parentFileId(s)
//                        .limit(100)
//                        .build();
//                CompletableFuture<org.zhinanzhen.b.dao.pojo.box.ListFileResponse> listFileResponseCompletableFuture = client.listFile(listFileRequest);
//                org.zhinanzhen.b.dao.pojo.box.ListFileResponse resp = listFileResponseCompletableFuture.get();
//                String json = new Gson().toJson(resp);
//                JsonNode jsonNode = new ObjectMapper().readTree(json);
//                JsonNode items = jsonNode.path("body").path("items");
//                for (JsonNode node : items) {
//                    boolean isFirstFolder = false;
//                    String fileId = node.get("fileId").asText();
//                    String type = node.get("type").asText();
//                    String parentFileId = node.get("parentFileId").asText();
//                    String name = node.get("name").asText();
////                    String driveId = node.get("driveId").asText();
////                    JsonNode creatorNameT = node.get("creatorName");
//                    Long fileSize = 0L;
//                    CloudDiskFile cloudDiskFileListByParentId = cloudDiskFileDAO.getById(null, null, s, null, null);
//                    if ("folder".equalsIgnoreCase(type)) {
//                        isFirstFolder = PatternMatcherUtil.containsPattern(name);
//                        if (cloudDiskFileListByParentId == null) {
//                            if (!isFirstFolder) {
//                                continue;
//                            }
//                        }
//                    }
//                    if ("file".equalsIgnoreCase(type)) {
//                        if (cloudDiskFileListByParentId == null) {
//                            continue;
//                        }
//                        fileSize = node.get("size").asLong();
//                    }
//                    CloudDiskFile cloudDiskFile = cloudDiskFileDAO.getById(null, null, fileId, null, null);
//                    Integer userId = 0;
//                    if (isFirstFolder) { // 获取userId
//                        String s1 = PatternMatcherUtil.getAllMatches(name).get(0);
//                        UserDO userById = userDAO.getUserById(Integer.parseInt(s1.substring(1)));
//                        if (userById != null) {
//                            userId = userById.getId();
//                        }
//                    } else {
//                        userId = cloudDiskFileListByParentId.getUserId();
//                    }
//                    if (type.equalsIgnoreCase("folder")) {
//                        newObjects.append(fileId).append(","); // 只收集新的 folderId
//                    }
//                    if (cloudDiskFile == null) {
//                        cloudDiskFile = CloudDiskFile.builder()
//                                .fileId(fileId)
//                                .parentFileId(parentFileId)
//                                .domainId("bj21743")
//                                .name(name)
//                                .type(type)
//                                .driveId(driveId)
//                                .adviserId(adviserId)
//                                .officialId(officialId)
//                                .fileSize(fileSize)
//                                .userId(userId)
//                                .build();
//                        if ("root".equalsIgnoreCase(parentFileId)) {
//                            cloudDiskFile.setRelativePath("/root" + "/" + name);
//                        } else {
//                            CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, parentFileId, null, null);
//                            cloudDiskFile.setRelativePath(byId.getRelativePath() + "/" + name);
//                        }
//                        if (adviserId != null) {
//                            AdviserDO adviserById = adviserDAO.getAdviserById(adviserId);
//                            cloudDiskFile.setOperator(adviserById.getName());
//                        }
//                        if (officialId != null) {
//                            OfficialDO officialById = officialDAO.getOfficialById(officialId);
//                            cloudDiskFile.setOperator(officialById.getName());
//                        }
//                        cloudDiskFileDAO.add(cloudDiskFile);
//                        cloudDiskFileList.add(cloudDiskFile);
//                    }
//                    if (PatternMatcherUtil.containsPattern(name)) {
//                        if (adviserId != null) {
//                            belongFolderMap = buildBelongFolder(userId, adviserId, officialId, "顾问资料", driveId, fileId, belongFolderMap);
//                        }
//                        if (officialId != null) {
//                            belongFolderMap = buildBelongFolder(userId, adviserId, officialId, "文案资料", driveId, fileId, belongFolderMap);
//                        }
//                    }
//                }
//                client.close();
//            } catch (IOException | ExecutionException | InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        try {
//            if (CollectionUtils.isNotEmpty(cloudDiskFileList)) {
//                List<CloudDiskFile> collect = cloudDiskFileList.stream().filter(cloudDiskFile -> "root".equalsIgnoreCase(cloudDiskFile.getParentFileId())).collect(Collectors.toList());
//                Map<String, CloudDiskFile> cloudDiskFileMap = collect.stream().collect(Collectors.toMap(CloudDiskFile::getFileId, Function.identity()));
//                for (CloudDiskFile cloudDiskFile : collect) {
//                    UserDO userDO = userDAO.getUserByName(cloudDiskFile.getName());
//                    if (ObjectUtil.isNotNull(userDO) && userDO.getName().equalsIgnoreCase(cloudDiskFile.getName())) {
//                        cloudDiskFile.setUserId(userDO.getId());
//                        cloudDiskFileDAO.update(cloudDiskFile);
//                    }
//                }
//                for (CloudDiskFile cloudDiskFile : cloudDiskFileList) {
//                    CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, cloudDiskFile.getParentFileId(), null, null);
//                    if (byId != null) {
//                        Integer userId = byId.getUserId();
//                        if (userId != null) {
//                            cloudDiskFile.setUserId(userId);
//                            cloudDiskFileDAO.update(cloudDiskFile);
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            cloudDiskFileList.clear();
//        } finally {
//            cloudDiskFileList.clear();
//        }
//        // 递归处理新发现的文件夹
//        if (newObjects.length() > 0) {
//            // 去掉末尾的 ","
//            newObjects = new StringBuilder(newObjects.substring(0, newObjects.length() - 1));
//        }
//        getFileStructure(String.valueOf(newObjects), adviserId, officialId, belongFolderMap, addCountMap, null);
//        return 0;
//    }

    @Override
    public int getFileStructure(String parentFileStructures, Integer adviserId, Integer officialId, Map<String, String> belongFolderMap, Map<String, Integer> addCountMap, String folderName, Integer userId, String synchronizeName) {
        UserCloud userCloud = cloudDiskFileDAO.getUserCloud(adviserId, officialId, null, null, null);
        if (folderName != null && parentFileStructures == null) {
            parentFileStructures = getParentFileId(folderName, userCloud.getDriveId());
        }
        String driveId = userCloud.getDriveId();
        if (parentFileStructures == null || parentFileStructures.trim().isEmpty()) {
            List<CloudDiskFile> cloudDiskFileList1 = cloudDiskFileDAO.listByParentFileId(null, null, null, null, userId, 0, 999);
            for (CloudDiskFile cloudDiskFile : cloudDiskFileList1) {
                try {
                    JsonNode body = wangPanUtils.getFile(cloudDiskFile.getDriveId(), cloudDiskFile.getFileId()).path("body");
                    String parentFileId = body.get("parentFileId").asText();
                    if (!cloudDiskFile.getParentFileId().equalsIgnoreCase(parentFileId)) {
                        cloudDiskFile.setIsDelete(1);
                        cloudDiskFileDAO.update(cloudDiskFile);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
            return 0; // 递归终止条件
        }
        String[] split = parentFileStructures.split(",");
        StringBuilder newObjects = new StringBuilder(); // 改用局部变量，避免全局污染
        belongFolderMap = new HashMap<>();
        if (folderName == null) {
            for (String s : split) {
                try {
                    JsonNode jsonNode = wangPanUtils.listFile(driveId, s);
                    JsonNode items = jsonNode.path("body").path("items");
                    for (JsonNode node : items) {
                        boolean isFirstFolder = false;
                        String fileId = node.get("fileId").asText();
                        String type = node.get("type").asText();
                        String parentFileId = node.get("parentFileId").asText();
                        String name = node.get("name").asText();
                        if ("folder".equalsIgnoreCase(type)) {
                            isFirstFolder = PatternMatcherUtil.containsPattern(name);
                            CloudDiskFile cloudDiskFileListByParentId = null;
                            if (isFirstFolder) {
                                String s1 = PatternMatcherUtil.getAllMatches(name);
                                UserDO userById = userDAO.getUserById(Integer.parseInt(s1));
                                String textBeforeAt = PatternMatcherUtil.getTextBeforeAt(name);
                                cloudDiskFileListByParentId = cloudDiskFileDAO.getById(null, null, null, textBeforeAt, userById.getId());
                                copyDataToPublic(cloudDiskFileListByParentId, userById, adviserId, officialId, fileId, driveId, true, parentFileStructures);
                            }
                        }
                        if ("file".equalsIgnoreCase(type)) {
                            continue;
                        }
                        if (isFirstFolder) { // 获取userId
                            String s1 = PatternMatcherUtil.getAllMatches(name);
                            UserDO userById = userDAO.getUserById(Integer.parseInt(s1));
                        }
                        if (type.equalsIgnoreCase("folder")) {
                            newObjects.append(fileId).append(","); // 只收集新的 folderId
                        }
                    }
                } catch (IOException | ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (synchronizeName != null) {
            try {
                JsonNode fileByName = wangPanUtils.getFileByName(driveId, synchronizeName);
                JsonNode items = fileByName.path("body").path("items").get(0);
                CloudDiskFile cloudDiskFile1 = wangPanUtils.buildCloudDiskFile(items);
                String s1 = PatternMatcherUtil.getAllMatches(folderName);
                UserDO userById = userDAO.getUserById(Integer.parseInt(s1));
                userId = userById.getId();
                if (officialId != null) {
                    OfficialDO officialById = officialDAO.getOfficialById(officialId);
                    cloudDiskFile1.setOperator(officialById.getName());
                }
                if (adviserId != null) {
                    AdviserDO adviserById = adviserDAO.getAdviserById(adviserId);
                    cloudDiskFile1.setOperator(adviserById.getName());
                }
                copyDataToPublic(cloudDiskFile1, userById, adviserId, officialId, cloudDiskFile1.getFileId(), driveId, false, parentFileStructures);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                JsonNode jsonNode = wangPanUtils.getFile(driveId, parentFileStructures);
                JsonNode items = jsonNode.path("body");
                CloudDiskFile cloudDiskFile = wangPanUtils.buildCloudDiskFile(items);
                String name = cloudDiskFile.getName();
                String s1 = PatternMatcherUtil.getAllMatches(name);
                UserDO userById = userDAO.getUserById(Integer.parseInt(s1));
                userId = userById.getId();
//                cloudDiskFile.setUserId(userById.getId());
//                cloudDiskFile.setOfficialId(officialId);
//                cloudDiskFile.setAdviserId(adviserId);
//                String textBeforeAt = PatternMatcherUtil.getTextBeforeAt(name);
                copyDataToPublic(null, userById, adviserId, officialId, cloudDiskFile.getFileId(), driveId, true, parentFileStructures);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (newObjects.length() > 0 && folderName == null) {
            // 去掉末尾的 ","
            newObjects = new StringBuilder(newObjects.substring(0, newObjects.length() - 1));
        }
        getFileStructure(String.valueOf(newObjects), adviserId, officialId, belongFolderMap, addCountMap, null, userId, null);
        return 0;
    }

    private void copyDataToPublic(CloudDiskFile cloudDiskFileListByParentId, UserDO userById, Integer adviserId,
                                  Integer officialId, String oldFileId, String oldDriverId, boolean isFirstFolder, String parentFileStructures) throws IOException, ExecutionException, InterruptedException {
        List<CloudDiskFile> cloudDiskFileList1 = new ArrayList<>();
        if (isFirstFolder) {
            if (cloudDiskFileListByParentId == null) {
                cloudDiskFileList1 = initializationFolder(userById.getId(), null, adviserId, officialId);
            } else {
                cloudDiskFileList1 = cloudDiskFileDAO.listByRelativePath(cloudDiskFileListByParentId.getRelativePath());
            }
            String secondFolder = "";
            for (CloudDiskFile cloudDiskFile : cloudDiskFileList1) {
                if (adviserId != null) {
                    secondFolder = "顾问资料";
                }
                if (officialId != null) {
                    secondFolder = "文案资料";
                }
                if (secondFolder.equalsIgnoreCase(cloudDiskFile.getName())) {
                    JsonNode jsonNode = wangPanUtils.listFile(oldDriverId, oldFileId);
                    JsonNode items = jsonNode.path("body").path("items");
                    for (JsonNode item : items) {
                        String name = item.get("name").asText();
                        CloudDiskFile byId = cloudDiskFileDAO.getById(null, cloudDiskFile.getFileId(), null, name, null);
                        if (byId != null) {
                            continue;
                        }
                        CloudDiskFile cloudDiskFile1 = wangPanUtils.buildCloudDiskFile(item);
                        wangPanUtils.copyFile(oldDriverId, "1020", cloudDiskFile1.getFileId(), cloudDiskFile.getFileId());
                    }
                } else {
                    continue;
                }
                getAllData(cloudDiskFile, null);
            }
        }
        if (!isFirstFolder) {
            JsonNode jsonNode = wangPanUtils.listFile(oldDriverId, oldFileId);
            JsonNode items = jsonNode.path("body").path("items");
            for (JsonNode item : items) {
                String name = item.get("name").asText();
                String fileId = item.get("fileId").asText();
                CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, null, name, userById.getId());
                if (byId != null) {
                    continue;
                }
                CloudDiskFile cloudDiskFile1 = wangPanUtils.buildCloudDiskFile(item);
                wangPanUtils.copyFile(oldDriverId, "1020", cloudDiskFile1.getFileId(), parentFileStructures);
            }
            cloudDiskFileListByParentId.setOfficialId(officialId);
            cloudDiskFileListByParentId.setAdviserId(adviserId);
            cloudDiskFileListByParentId.setUserId(userById.getId());
            CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, parentFileStructures, null, cloudDiskFileListByParentId.getUserId());
            getAllData(byId, null);
        }
    }

    private void getAllData(CloudDiskFile cloudDiskFile, String parentFileStructures) {
        try {
            if (parentFileStructures == null) {
                StringBuilder newObjects = new StringBuilder(); // 改用局部变量，避免全局污染
                CloudDiskFile cloudDiskFileT = null;
                if (cloudDiskFile != null) {
                    JsonNode jsonNode = wangPanUtils.listFile(cloudDiskFile.getDriveId(), cloudDiskFile.getFileId());
                    JsonNode items = jsonNode.path("body").path("items");
                    for (JsonNode item : items) {
                        cloudDiskFileT = wangPanUtils.buildCloudDiskFile(item);
                        cloudDiskFileT.setAdviserId(cloudDiskFile.getAdviserId());
                        cloudDiskFileT.setOfficialId(cloudDiskFile.getOfficialId());
                        cloudDiskFileT.setRelativePath(cloudDiskFile.getRelativePath() + "/" + cloudDiskFileT.getName());
                        cloudDiskFileT.setUserId(cloudDiskFile.getUserId());
                        cloudDiskFileT.setOperator(cloudDiskFile.getOperator());
                        CloudDiskFile byId = cloudDiskFileDAO.getById(null, cloudDiskFileT.getParentFileId(), cloudDiskFileT.getFileId(), cloudDiskFileT.getName(), cloudDiskFileT.getUserId());
                        if (byId == null) {
                            cloudDiskFileDAO.add(cloudDiskFileT);
                        }
                        if (cloudDiskFileT.getType().equalsIgnoreCase("folder")) {
                            newObjects.append(cloudDiskFileT.getFileId()).append(","); // 只收集新的 folderId
                        }
                    }
                }
                // 递归处理新发现的文件夹
                if (newObjects.length() > 0) {
                    // 去掉末尾的 ","
                    newObjects = new StringBuilder(newObjects.substring(0, newObjects.length() - 1));
                    getAllData(cloudDiskFileT, String.valueOf(newObjects));
                }
            }
            if (!StringUtils.isEmpty(parentFileStructures)) {
                String[] split = parentFileStructures.split(",");
                StringBuilder newObjects = new StringBuilder(); // 改用局部变量，避免全局污染
                CloudDiskFile cloudDiskFileT = null;
                for (String s : split) {
                    JsonNode jsonNode = wangPanUtils.listFile(cloudDiskFile.getDriveId(), s);
                    JsonNode items = jsonNode.path("body").path("items");
                    for (JsonNode node : items) {
                        cloudDiskFileT = wangPanUtils.buildCloudDiskFile(node);
                        cloudDiskFileT.setAdviserId(cloudDiskFile.getAdviserId());
                        cloudDiskFileT.setOfficialId(cloudDiskFile.getOfficialId());
                        CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, s, null, null);
                        cloudDiskFileT.setRelativePath(byId.getRelativePath() + "/" + cloudDiskFileT.getName());
                        cloudDiskFileT.setUserId(byId.getUserId());
                        cloudDiskFileT.setOperator(byId.getOperator());
                        cloudDiskFileDAO.add(cloudDiskFileT);
                        if (cloudDiskFileT.getType().equalsIgnoreCase("folder")) {
                            newObjects.append(cloudDiskFileT.getFileId()).append(","); // 只收集新的 folderId
                        }
                    }
                }
                // 递归处理新发现的文件夹
                if (newObjects.length() > 0) {
                    // 去掉末尾的 ","
                    newObjects = new StringBuilder(newObjects.substring(0, newObjects.length() - 1));
                    getAllData(cloudDiskFileT, String.valueOf(newObjects));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                // 如果是唯一键冲突异常，记录日志并继续执行
                log.warn("重复的parent_file_id和file_id组合，跳过处理: {}", parentFileStructures);
                // 可以选择更新现有记录而不是插入
                // yourRepository.updateByParentFileIdAndFileId(entity);
            } else {
                // 其他类型的异常重新抛出
                throw e;
            }
        }
    }


    private String getParentFileId(String folderName, String driveId) {
        folderName = "name=\"" + folderName + "\"";
        String parentFileId = null;
        AsyncClient asyncClient = getAsyncClient();
        SearchFileRequest build = SearchFileRequest.builder()
                .driveId(driveId)
                .query(folderName)
                .build();
        CompletableFuture<SearchFileResponse> file = asyncClient.searchFile(build);
        try {
            SearchFileResponse searchFileResponse = file.get();
            String json = new Gson().toJson(searchFileResponse);
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            JsonNode items = jsonNode.path("body").path("items");
            for (JsonNode item : items) {
                parentFileId = item.get("fileId").asText();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return parentFileId;
    }

//    @Override
//    public List<CloudDiskFile> initializationFolder(Integer userId, Integer applicantId, Integer adviserId, Integer officialId) throws ExecutionException, InterruptedException {
//        UserCloud userCloud = cloudDiskFileDAO.getUserCloud(adviserId, officialId);
//        if (userCloud == null) {
//            return null;
//        }
//        String driveId = userCloud.getDriveId();
//        AsyncClient client = getAsyncClient();
//        try {
//            // Parameter settings for API request
//            ListFileRequest listFileRequest = ListFileRequest.builder()
//                    .parentFileId("root")
//                    .driveId(driveId)
//                    // Request-level configuration rewrite, can set Http request parameters, etc.
//                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
//                    .build();
//
//            // Asynchronously get the return value of the API request
//            CompletableFuture<com.aliyun.sdk.service.pds20220301.models.ListFileResponse> response = client.listFile(listFileRequest);
//            // Synchronously get the return value of the API request
//            com.aliyun.sdk.service.pds20220301.models.ListFileResponse resp = response.get();
//            String json = new Gson().toJson(resp);
//        } catch (ExecutionException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return null;
//    }


    @Override
    public List<CloudDiskFile> initializationFolder(Integer userId, Integer applicantId, Integer adviserId, Integer officialId) {
        log.info("key值为----------------------{}", ACCESS_KEY_ID);
        List<CloudDiskFile> cloudDiskFileList = new ArrayList<>();
        cloudDiskFileList = cloudDiskFileDAO.listByParentFileId(null, null, null, applicantId, userId, 0, 20);
        int add = -1;
        try {
            if (cloudDiskFileList.isEmpty()) {
                CloudDiskFile cloudDiskFile = new CloudDiskFile();
                // 创建上传文件的请求并获取上传链接
                // Configure Credentials authentication information, including ak, secret, token
                AsyncClient client = getAsyncClient();
                UserDO userById = userDAO.getUserById(userId);
                CreateFileRequest createFileRequest = null;
                createFileRequest = CreateFileRequest.builder()
                        .name(userById.getName())
                        .type("folder")
                        .parentFileId("root")
                        .driveId("1020")
                        .build();
                // Asynchronously get the return value of the API request
                CompletableFuture<CreateFileResponse> response = client.createFile(createFileRequest);
                // Synchronously get the return value of the API request
                CreateFileResponse resp = response.get();
                System.out.println(new Gson().toJson(resp));
                // Asynchronous processing of return values
                String json = new Gson().toJson(resp);
                JSONObject jsonObject = JSON.parseObject(json);
                JSONObject body1 = jsonObject.getJSONObject("body");
                String fileId = body1.get("fileId").toString();

                cloudDiskFile = CloudDiskFile.builder().fileId(fileId).parentFileId("root").
                        domainId("bj21743").name(userById.getName()).type("folder").driveId("1020").userId(userId).applicantId(applicantId)
                        .adviserId(adviserId).relativePath("/root" + "/" + userById.getName()).officialId(officialId).build();
                if (adviserId != null) {
                    AdviserDO adviserById = adviserDAO.getAdviserById(adviserId);
                    cloudDiskFile.setOperator(adviserById.getName());
                }
                if (officialId != null) {
                    OfficialDO officialById = officialDAO.getOfficialById(officialId);
                    cloudDiskFile.setOperator(officialById.getName());
                }
                cloudDiskFileDAO.add(cloudDiskFile);
                cloudDiskFileList.add(cloudDiskFile);

                // 创建客户文件夹下面的顾问资料文件夹和文案资料文件夹
                createFileRequest = CreateFileRequest.builder()
                        .name("顾问资料")
                        .type("folder")
                        .parentFileId(fileId)
                        .driveId("1020")
                        .build();
                // Asynchronously get the return value of the API request
                CompletableFuture<CreateFileResponse> responseT = client.createFile(createFileRequest);
                // Synchronously get the return value of the API request
                CreateFileResponse respT = responseT.get();
                System.out.println(new Gson().toJson(respT));
                // Asynchronous processing of return values
                String jsonT = new Gson().toJson(respT);
                JSONObject jsonObjectT = JSON.parseObject(jsonT);
                JSONObject bodyT = jsonObjectT.getJSONObject("body");
                String fileIdT = bodyT.get("fileId").toString();

                cloudDiskFile = CloudDiskFile.builder().fileId(fileIdT).parentFileId(fileId).
                        domainId("bj21743").name("顾问资料").type("folder").driveId("1020").userId(userId)
                        .applicantId(applicantId).adviserId(adviserId).officialId(officialId).build();
                if ("root".equalsIgnoreCase(fileId)) {
                    cloudDiskFile.setRelativePath("/root" + "/" + "顾问资料");
                } else {
                    CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, fileId, null, null);
                    cloudDiskFile.setRelativePath(byId.getRelativePath() + "/" + "顾问资料");
                }
                if (adviserId != null) {
                    AdviserDO adviserById = adviserDAO.getAdviserById(adviserId);
                    cloudDiskFile.setOperator(adviserById.getName());
                }
                if (officialId != null) {
                    OfficialDO officialById = officialDAO.getOfficialById(officialId);
                    cloudDiskFile.setOperator(officialById.getName());
                }
                cloudDiskFileList.add(cloudDiskFile);
                cloudDiskFileDAO.add(cloudDiskFile);

                createFileRequest = CreateFileRequest.builder()
                        .name("文案资料")
                        .type("folder")
                        .parentFileId(fileId)
                        .driveId("1020")
                        .build();
                // Asynchronously get the return value of the API request
                CompletableFuture<CreateFileResponse> responseW = client.createFile(createFileRequest);
                // Synchronously get the return value of the API request
                CreateFileResponse respW = responseW.get();
                System.out.println(new Gson().toJson(respW));
                // Asynchronous processing of return values
                String jsonW = new Gson().toJson(respW);
                JSONObject jsonObjectW = JSON.parseObject(jsonW);
                JSONObject bodyW = jsonObjectW.getJSONObject("body");
                String fileIdW = bodyW.get("fileId").toString();
                client.close();

                cloudDiskFile = CloudDiskFile.builder().fileId(fileIdW).parentFileId(fileId).
                        domainId("bj21743").name("文案资料").type("folder").driveId("1020").userId(userId)
                        .applicantId(applicantId).adviserId(adviserId).officialId(officialId).build();
                if ("root".equalsIgnoreCase(fileId)) {
                    cloudDiskFile.setRelativePath("/root" + "/" + "文案资料");
                } else {
                    CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, fileId, null, null);
                    cloudDiskFile.setRelativePath(byId.getRelativePath() + "/" + "文案资料");
                }
                if (adviserId != null) {
                    AdviserDO adviserById = adviserDAO.getAdviserById(adviserId);
                    cloudDiskFile.setOperator(adviserById.getName());
                }
                if (officialId != null) {
                    OfficialDO officialById = officialDAO.getOfficialById(officialId);
                    cloudDiskFile.setOperator(officialById.getName());
                }
                cloudDiskFileList.add(cloudDiskFile);
                cloudDiskFileDAO.add(cloudDiskFile);
            } else {
                String parentFildT = new String();
                List<CloudDiskFile> collect = cloudDiskFileList.stream().sorted(Comparator.comparing(p -> "root".equalsIgnoreCase(p.getParentFileId()) ? 0 : 1)).collect(Collectors.toList());
                for (CloudDiskFile cloudDiskFile : collect) {
                    if (!"root".equalsIgnoreCase(cloudDiskFile.getParentFileId()) && !Objects.equals(parentFildT, "")) {
                        cloudDiskFile.setParentFileId(parentFildT);
                    }
                    AsyncClient client = getAsyncClient();
                    // Parameter settings for API request
                    GetFileRequest getFileRequest = GetFileRequest.builder()
                            .driveId("1020")
                            .fileId(cloudDiskFile.getFileId())
                            // Request-level configuration rewrite, can set Http request parameters, etc.
                            // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                            .build();
                    try {
                        // Asynchronously get the return value of the API request
                        CompletableFuture<GetFileResponse> response = client.getFile(getFileRequest);
                        // Synchronously get the return value of the API request
                        GetFileResponse resp = response.get();
                        String json = new Gson().toJson(resp);
                        JSONObject jsonObject = JSON.parseObject(json);
                        String string = jsonObject.get("statusCode").toString();
                        if ("200".equalsIgnoreCase(string)) {
                            continue;
                        }
                    } catch (Exception e) {
                        AsyncClient clientT = getAsyncClient();
                        CreateFileRequest createFileRequest = CreateFileRequest.builder()
                                .name(cloudDiskFile.getName())
                                .type("folder")
                                .parentFileId(cloudDiskFile.getParentFileId())
                                .driveId("1020")
                                .build();
                        // Asynchronously get the return value of the API request
                        CompletableFuture<CreateFileResponse> response = clientT.createFile(createFileRequest);
                        // Synchronously get the return value of the API request
                        CreateFileResponse resp = response.get();
                        System.out.println(new Gson().toJson(resp));
                        // Asynchronous processing of return values
                        String json = new Gson().toJson(resp);
                        JSONObject jsonObject = JSON.parseObject(json);
                        JSONObject body1 = jsonObject.getJSONObject("body");
                        String fileId = body1.get("fileId").toString();
                        cloudDiskFile.setFileId(fileId);
                        cloudDiskFile.setAdviserId(adviserId);
                        cloudDiskFile.setOfficialId(officialId);
                        cloudDiskFileDAO.update(cloudDiskFile);
                        if ("root".equalsIgnoreCase(cloudDiskFile.getParentFileId())) {
                            parentFildT = cloudDiskFile.getFileId();
                        }
                        clientT.close();
                    }
                }
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return cloudDiskFileList;
    }

    @Override
    public int update(String fileId, String type, Integer userId, Integer applicantId, Integer adviserId, Integer id, String name, Integer officialId, String relativePath) {
        CloudDiskFile cloudDiskFile = cloudDiskFileDAO.getById(id, null, null, null, null);
        AsyncClient client = getAsyncClient();
        String oldPart = cloudDiskFile.getName();
        String oldRelativePath = cloudDiskFile.getRelativePath();
        try {
            // Parameter settings for API request
            GetFileRequest getFileRequest = GetFileRequest.builder()
                    .driveId("1020")
                    .fileId(cloudDiskFile.getFileId())
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();
            CompletableFuture<GetFileResponse> response = client.getFile(getFileRequest);
//            GetFileResponse resp = response.get();
//            String json = new Gson().toJson(resp);
//            JSONObject jsonObject = JSON.parseObject(json);
            JSONObject jsonObject = convertToJsonObject(response);
            String string = jsonObject.get("statusCode").toString();
            if (!"200".equalsIgnoreCase(string)) {
                return -1;
            }
            // Parameter settings for API request
            UpdateFileRequest updateFileRequest = UpdateFileRequest.builder()
                    .driveId("1020")
                    .fileId(fileId)
                    .name(name)
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();

            // Asynchronously get the return value of the API request
            CompletableFuture<UpdateFileResponse> responseT = client.updateFile(updateFileRequest);
            JSONObject jsonObjectT = convertToJsonObject(responseT);
            // Synchronously get the return value of the API request
            String string1 = jsonObjectT.get("statusCode").toString();
            if ("200".equalsIgnoreCase(string1)) {
                cloudDiskFile.setName(name);
                cloudDiskFile.setFileId(fileId);
                CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, cloudDiskFile.getParentFileId(), null, null);
                String relativePath1 = byId.getRelativePath();
                String s = relativePath1 + "/" + name;
                cloudDiskFile.setRelativePath(s);
                cloudDiskFileDAO.update(cloudDiskFile);
                if ("folder".equalsIgnoreCase(cloudDiskFile.getType())) {
                    List<CloudDiskFile> cloudDiskFileList1 = cloudDiskFileDAO.listByRelativePath(oldRelativePath);
                    if (CollectionUtils.isNotEmpty(cloudDiskFileList1)) {
                        for (CloudDiskFile diskFile : cloudDiskFileList1) {
                            String relativePath2 = diskFile.getRelativePath();
                            if (relativePath2.contains(oldPart)) {
                                String s1 = relativePath2.replaceAll("(?<=/)" + Pattern.quote(oldPart) + "(?=/|$)", cloudDiskFile.getName());
                                diskFile.setRelativePath(s1);
                                cloudDiskFileDAO.update(diskFile);
                            }
                        }
                    }
                }
                return 1;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private <T> JSONObject convertToJsonObject(CompletableFuture<T> response) throws ExecutionException, InterruptedException {
        T resp = response.get();
        String json = new Gson().toJson(resp);
        return JSON.parseObject(json);
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
                                .setEndpointOverride("bj21743.api.aliyunpds.com")
                        //.setConnectTimeout(Duration.ofSeconds(30))
                )
                .build();
        return client;
    }

    private String getDownloadUrl(String fileId) {
        try {
            AsyncClient asyncClient = getAsyncClient();
            GetFileRequest getFileRequest = GetFileRequest.builder()
                    .driveId("1020")
                    .fileId(fileId)
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();
            CompletableFuture<GetFileResponse> file = asyncClient.getFile(getFileRequest);
            GetFileResponse getFileResponse = file.get();
            String json = new Gson().toJson(getFileResponse);
            JSONObject jsonObject = JSON.parseObject(json);
            JSONObject body1 = jsonObject.getJSONObject("body");
            return body1.get("downloadUrl").toString();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> buildBelongFolder(Integer userId, Integer adviserId, Integer officialId, String folderName, String driverId, String parentFileId, Map<String, String> belongFolderMap) throws ExecutionException, InterruptedException, IOException {
        CloudDiskFile cloudDiskFile = null;
        AsyncClient client = getAsyncClient();
        ListFileRequest build = ListFileRequest.builder()
                .driveId(driverId)
                .parentFileId(parentFileId)
                .build();
        CompletableFuture<com.aliyun.sdk.service.pds20220301.models.ListFileResponse> listFileResponseCompletableFuture = client.listFile(build);
        com.aliyun.sdk.service.pds20220301.models.ListFileResponse listFileResponse = listFileResponseCompletableFuture.get();
        String json = new Gson().toJson(listFileResponse);
        JsonNode jsonNode = new ObjectMapper().readTree(json);
        JsonNode items = jsonNode.path("body").path("items");
        for (JsonNode item : items) {
            String name = item.get("name").asText();
            String type = item.get("type").asText();
            String fileId = item.get("fileId").asText();
            if (("folder".equalsIgnoreCase(type)) && name.equalsIgnoreCase(folderName)) {
                cloudDiskFile = cloudDiskFileDAO.getById(null, parentFileId, fileId, folderName, null);
                if (cloudDiskFile != null) {
                    belongFolderMap.put(parentFileId, fileId);
                    return belongFolderMap;
                }
                cloudDiskFile = cloudDiskFileDAO.getById(null, parentFileId, null, folderName, null);
                if (cloudDiskFile != null && !cloudDiskFile.getFileId().equalsIgnoreCase(fileId)) {
                    cloudDiskFile.setFileId(fileId);
                    cloudDiskFileDAO.update(cloudDiskFile);
                    belongFolderMap.put(parentFileId, fileId);
                    return belongFolderMap;
                }
            }
        }
        // 创建上传文件的请求并获取上传链接
        CreateFileRequest createFileRequest = null;

        // 创建客户文件夹下面的顾问资料文件夹和文案资料文件夹
        createFileRequest = CreateFileRequest.builder()
                .name(folderName)
                .type("folder")
                .parentFileId(parentFileId)
                .driveId(driverId)
                .build();
        // Asynchronously get the return value of the API request
        CompletableFuture<CreateFileResponse> responseT = client.createFile(createFileRequest);
        // Synchronously get the return value of the API request
        CreateFileResponse respT = responseT.get();
        System.out.println(new Gson().toJson(respT));
        // Asynchronous processing of return values
        String jsonT = new Gson().toJson(respT);
        JSONObject jsonObjectT = JSON.parseObject(jsonT);
        JSONObject bodyT = jsonObjectT.getJSONObject("body");
        String fileIdT = bodyT.get("fileId").toString();

        cloudDiskFile = CloudDiskFile.builder().fileId(fileIdT).parentFileId(parentFileId).
                domainId("bj21743").name(folderName).type("folder").driveId(driverId).userId(userId)
                .applicantId(0).adviserId(adviserId).officialId(officialId).build();
        if ("root".equalsIgnoreCase(parentFileId)) {
            cloudDiskFile.setRelativePath("/root" + "/" + folderName);
        } else {
            CloudDiskFile byId = cloudDiskFileDAO.getById(null, null, parentFileId, null, null);
            cloudDiskFile.setRelativePath(byId.getRelativePath() + "/" + folderName);
        }
        if (adviserId != null) {
            AdviserDO adviserById = adviserDAO.getAdviserById(adviserId);
            cloudDiskFile.setOperator(adviserById.getName());
        }
        if (officialId != null) {
            OfficialDO officialById = officialDAO.getOfficialById(officialId);
            cloudDiskFile.setOperator(officialById.getName());
        }
        belongFolderMap.put(parentFileId, fileIdT);
        cloudDiskFileList.add(cloudDiskFile);
        cloudDiskFileDAO.add(cloudDiskFile);

        return belongFolderMap;
    }

    public String getInsertName(String path) {

        String prefix = "/root/";

        // 检查是否包含前缀
        if (!path.startsWith(prefix)) {
            return "路径格式不正确";
        }

        int startIndex = prefix.length();

        // 从startIndex开始查找下一个斜杠
        int endIndex = path.indexOf('/', startIndex);

        if (endIndex == -1) {
            // 如果没有找到下一个斜杠，返回从startIndex到末尾的内容
            return path.substring(startIndex);
        } else {
            // 如果找到了斜杠，返回两个斜杠之间的内容
            return path.substring(startIndex, endIndex);
        }
    }
}
