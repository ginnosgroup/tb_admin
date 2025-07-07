package org.zhinanzhen.b.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.pds20220301.AsyncClient;
import com.aliyun.sdk.service.pds20220301.models.*;
import com.fasterxml.jackson.core.JsonParseException;
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
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.CloudDiskFileDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.box.ListFileResponse;
import org.zhinanzhen.b.dao.pojo.box.MyAsyncClient;
import org.zhinanzhen.b.service.CloudDiskService;
import org.zhinanzhen.b.service.UploadResponseData;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.b.service.pojo.PartInfo;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
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

    List<CloudDiskFile> cloudDiskFileList = new ArrayList<>();

    @Override
    public int addAndUpdate(MultipartFile file, String type, int applicantId,Integer userId, String parentFileId,
                            Integer adviserId, Integer id, String folderName, Integer officialId) {
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
                cloudDiskFile = cloudDiskFileDAO.getById(id, parentFileId, null, folderName);
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
                            domainId("bj21743").name(folderName).type(type).driveId("1020").userId(userId).applicantId(applicantId).adviserId(adviserId).officialId(officialId).build();
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
                cloudDiskFile = cloudDiskFileDAO.getById(id, null, null, folderName);
                if (id != null) {
                    if (cloudDiskFile.getName().equals(fileName)) {
                        return -1;
                    }
                    createFileRequest = CreateFileRequest.builder()
                            .name(fileName)
                            .type(type)
                            .parentFileId(parentFileId)
                            .driveId("1020")
                            .size(fileTmp.length())
                            .fileId(cloudDiskFile.getFileId())
                            .partInfoList(java.util.Arrays.asList(
                                    partInfoList0
                            ))
                            .build();
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
                            domainId("bj21743").name(fileName).type(type).driveId(driveId).applicantId(applicantId).userId(userId).adviserId(adviserId).officialId(officialId).build();
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
    public int deleteById(Integer id, String fileId) {
        CloudDiskFile cloudDiskFile = cloudDiskFileDAO.getById(id, null, null, null);
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
        List<CloudDiskFile> cloudDiskFileList = cloudDiskFileDAO.listByParentFileId(id, parentFileId, name, applicantId, userId, pageNum, pageSize);
        for (CloudDiskFile cloudDiskFile : cloudDiskFileList) {
            String name1 = cloudDiskFile.getName();
            if (name1.contains("顾问")) {
                AdviserDO adviserById = adviserDAO.getAdviserById(cloudDiskFile.getAdviserId());
                if (ObjectUtil.isNotNull(adviserById)) {
                    cloudDiskFile.setOperator(adviserById.getName());
                }
            }
            if (name1.contains("文案")) {
                Integer officialId = cloudDiskFile.getOfficialId();
                if (officialId != null) {
                    OfficialDO officialById = officialDAO.getOfficialById(cloudDiskFile.getOfficialId());
                    if (ObjectUtil.isNotNull(officialById)) {
                        cloudDiskFile.setOperator(officialById.getName());
                    }
                }
            }
        }
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
            CloudDiskFile cloudDiskFile = cloudDiskFileDAO.getById(null, parentFileId, null, null);
            cloudDiskFile.setUrl(shareUrl);
            cloudDiskFileDAO.update(cloudDiskFile);
            return shareUrl;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getFileStructure(String parentFileStructures) {
        if (parentFileStructures == null || parentFileStructures.trim().isEmpty()) {
            return 0; // 递归终止条件
        }
        String[] split = parentFileStructures.split(",");
        StringBuilder newObjects = new StringBuilder(); // 改用局部变量，避免全局污染
        for (String s : split) {
            try {
                StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                        .accessKeyId("LTAI5tLov73MZ92VARfLNgrH")
                        .accessKeySecret("gLYhLvlnpb1OHGaEBbyD94B9QfJRWe")
                        .build());

                MyAsyncClient client = MyAsyncClient.builder()
                        .region("cn-beijing")
                        .credentialsProvider(provider)
                        .overrideConfiguration(
                                ClientOverrideConfiguration.create()
                                        .setEndpointOverride("bj21743.api.aliyunpds.com")
                        )
                        .build();
                ListFileRequest listFileRequest = ListFileRequest.builder()
                        .driveId("1020")
                        .parentFileId(s)
                        .limit(100)
                        .build();
                CompletableFuture<org.zhinanzhen.b.dao.pojo.box.ListFileResponse> listFileResponseCompletableFuture = client.listFile(listFileRequest);
                org.zhinanzhen.b.dao.pojo.box.ListFileResponse resp = listFileResponseCompletableFuture.get();
                String json = new Gson().toJson(resp);
                JsonNode jsonNode = new ObjectMapper().readTree(json);
                JsonNode items = jsonNode.path("body").path("items");
                for (JsonNode node : items) {
                    Integer adviserId = 0;
                    Integer officialId = 0;
                    String fileId = node.get("fileId").asText();
                    String type = node.get("type").asText();
                    String parentFileId = node.get("parentFileId").asText();
                    String name = node.get("name").asText();
                    String driveId = node.get("driveId").asText();
                    JsonNode creatorNameT = node.get("creatorName");
                    String creatorName = "";
                    if (creatorNameT != null) {
                        creatorName = creatorNameT.asText();
                    }
                    List<AdviserDO> adviserDOS = adviserDAO.listAdviser(creatorName, null, 0, 20);
                    List<OfficialDO> officialDOS = officialDAO.getOfficialByName(creatorName);
                    if (adviserDOS != null && adviserDOS.size() > 0) {
                        adviserId = adviserDOS.get(0).getId();
                    }
                    if (adviserDOS != null && officialDOS.size() > 0) {
                        officialId = officialDOS.get(0).getId();
                    }
                    CloudDiskFile cloudDiskFile = cloudDiskFileDAO.getById(null, null, fileId, null);
                    if (type.equalsIgnoreCase("folder")) {
                        newObjects.append(fileId).append(","); // 只收集新的 folderId
                    }
                    if (cloudDiskFile == null) {
                        cloudDiskFile = CloudDiskFile.builder()
                                .fileId(fileId)
                                .parentFileId(parentFileId)
                                .domainId("bj21743")
                                .name(name)
                                .type(type)
                                .driveId(driveId)
                                .adviserId(adviserId)
                                .officialId(officialId)
                                .build();
                        cloudDiskFileDAO.add(cloudDiskFile);
                        cloudDiskFileList.add(cloudDiskFile);
                    }
                }
                client.close();
            } catch (IOException | ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // 递归处理新发现的文件夹
        if (newObjects.length() > 0) {
            // 去掉末尾的 ","
            String nextLevelFolders = newObjects.substring(0, newObjects.length() - 1);
            getFileStructure(nextLevelFolders);
        }
        try {
            if (CollectionUtils.isNotEmpty(cloudDiskFileList)) {
                List<CloudDiskFile> collect = cloudDiskFileList.stream().filter(cloudDiskFile -> "root".equalsIgnoreCase(cloudDiskFile.getParentFileId())).collect(Collectors.toList());
                Map<String, CloudDiskFile> cloudDiskFileMap = collect.stream().collect(Collectors.toMap(CloudDiskFile::getFileId, Function.identity()));
                for (CloudDiskFile cloudDiskFile : collect) {
                    UserDO userDO = userDAO.getUserByName(cloudDiskFile.getName());
                    if (ObjectUtil.isNotNull(userDO) && userDO.getName().equalsIgnoreCase(cloudDiskFile.getName())) {
                        cloudDiskFile.setUserId(userDO.getId());
                        cloudDiskFileDAO.update(cloudDiskFile);
                    }
                }
                for (CloudDiskFile cloudDiskFile : cloudDiskFileList) {
                    CloudDiskFile cloudDiskFileT = cloudDiskFileMap.get(cloudDiskFile.getParentFileId());
                    if (cloudDiskFileT != null) {
                        Integer userId = cloudDiskFileT.getUserId();
                        if (userId != null) {
                            cloudDiskFile.setUserId(userId);
                            cloudDiskFileDAO.update(cloudDiskFile);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            cloudDiskFileList.clear();
        } finally {
            cloudDiskFileList.clear();
        }
        return 0;
    }

    @Override
    public List<CloudDiskFile> initializationFolder(Integer userId, Integer applicantId, Integer adviserId, Integer officialId) {
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
                        domainId("bj21743").name(userById.getName()).type("folder").driveId("1020").userId(userId).applicantId(applicantId).adviserId(adviserId).officialId(officialId).build();
                cloudDiskFileDAO.add(cloudDiskFile);

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
                        domainId("bj21743").name("顾问资料").type("folder").driveId("1020").userId(userId).applicantId(applicantId).adviserId(adviserId).officialId(officialId).build();
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
                        domainId("bj21743").name("文案资料").type("folder").driveId("1020").userId(userId).applicantId(applicantId).adviserId(adviserId).officialId(officialId).build();
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
    public int update(String fileId, String type, Integer userId, Integer applicantId, Integer adviserId, Integer id, String name, Integer officialId) {
        CloudDiskFile cloudDiskFile = cloudDiskFileDAO.getById(id, null, null, null);
        AsyncClient client = getAsyncClient();
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
                cloudDiskFileDAO.update(cloudDiskFile);
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
}
