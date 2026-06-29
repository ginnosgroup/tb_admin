package org.zhinanzhen.b.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.darabonba.stream.Client;
import com.aliyun.sdk.service.pds20220301.AsyncClient;
import com.aliyun.sdk.service.pds20220301.models.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.PassportOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.PassportOCRResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ServiceDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderApplicantDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.b.dao.pojo.IdentifyingInformationDO;
import org.zhinanzhen.b.dao.pojo.customer.MainInformation;
import org.zhinanzhen.b.service.CustomerInformationService;
import org.zhinanzhen.b.service.UploadResponseData;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.b.service.pojo.PartInfo;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.tb.utils.AESUtils;
import org.zhinanzhen.tb.utils.Base64Util;
import org.zhinanzhen.tb.utils.WebDavUtils;

import darabonba.core.client.ClientOverrideConfiguration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service("CustomerInformationService")
public class CustomerInformationServiceImpl extends BaseService implements CustomerInformationService {
    @Resource
    private CustomerInformationDAO customerInformationDAO;

    @Resource
    private ServiceOrderDAO serviceOrderDAO;

    @Resource
    private ServiceOrderApplicantDAO serviceOrderApplicantDao;

    @Resource
    OfficialDAO officialDAO;

    @Resource
    private ApplicantDAO applicantDao;

    @Resource
    private ServiceDAO serviceDao;

    @Resource
    private AdviserDAO adviserDao;

    @Resource
    private CloudDiskFileDAO cloudDiskFileDAO;

    @Value("${tencent.SecretId}")
    private String secretId;

    @Value("${tencent.SecretKey}")
    private String secretKey;

    @Value("${aliyun.ACCESSKEYID}")
    private String aliyunAccessKeyId;

    @Value("${aliyun.ACCESSKEYSECRET}")
    private String aliyunAccessKeySecret;

    @Value("${aliyun.PDSENDPOINT}")
    private String aliyunPdsEndpoint;

    @Value("${aliyun.PDSDOMAINID}")
    private String aliyunPdsDomainId;

    @Override
    public void add(CustomerInformationDO customerInformationDO) throws ServiceException {
        if(customerInformationDO.getMainInformation().getFamilyName().contains(" ")){
            ServiceException se = new ServiceException("保存失败！客户姓有空格符，请修改后重新保存!");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            Map<String, Object> webdavResult = webdav(customerInformationDO);
            String folderFileId = (String) webdavResult.get("folderFileId");
            customerInformationDO.setMmdiskPath(folderFileId);
            customerInformationDAO.insert(customerInformationDO);

            // 保存CloudDiskFile记录
            @SuppressWarnings("unchecked")
            List<CloudDiskFile> cloudDiskFiles = (List<CloudDiskFile>) webdavResult.get("cloudDiskFiles");
            if (cloudDiskFiles != null && !cloudDiskFiles.isEmpty()) {
                ServiceOrderDO serviceOrder = serviceOrderDAO.getServiceOrderById(customerInformationDO.getServiceOrderId());
                for (CloudDiskFile cdf : cloudDiskFiles) {
                    cdf.setUserId(serviceOrder.getUserId());
                    cdf.setApplicantId(serviceOrder.getApplicantId());
                    cdf.setAdviserId(serviceOrder.getAdviserId());
                    cdf.setOfficialId(serviceOrder.getOfficialId());
                    cloudDiskFileDAO.add(cdf);
                }
            }

            sendRemind(customerInformationDO.getServiceOrderId());
            deleteAll(customerInformationDO);
        } catch (Exception e) {
            e.printStackTrace();
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public CustomerInformationDO get(int id) throws ServiceException {
        try {
//            CustomerInformationDO customerInformationDO=null;
//             customerInformationDO = customerInformationDAO.getByServiceOrderId(id);
            CustomerInformationDO customerInformationDO = customerInformationDAO.getByServiceOrderId(id);
            if (ObjectUtil.isNotNull(customerInformationDO)&&customerInformationDO.getMmdiskPath() != null) {
                String mmdiskPath = customerInformationDO.getMmdiskPath().replace("\"", "");
                customerInformationDO.setMmdiskPath(mmdiskPath);
            }
            return customerInformationDO;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }

    }

    @Override
    public void update(CustomerInformationDO record) throws ServiceException {
        try {
            customerInformationDAO.update(record);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public void delete(int id) throws ServiceException {
        try {
            customerInformationDAO.delete(id);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public CustomerInformationDO getByServiceOrderId(int serviceOrderId) throws ServiceException {
        try {
            return customerInformationDAO.getByServiceOrderId(serviceOrderId);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public String upload(String familyName,String givenName,String name, MultipartFile file) throws IOException, ServiceException {
        if (file == null) {
            ServiceException se = new ServiceException("上传文件为空!");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            String rFamilyName = familyName.replace(" ", "");
            String rgivenName = givenName.replace(" ", "");
            LocalDate date = LocalDate.now(); // get the current date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            String formatdate = date.format(formatter);
            String dir = "/uploads/customerInformation/"+rFamilyName.toUpperCase() +"_"+ rgivenName.toUpperCase() + "/";
            String fileName = file.getOriginalFilename().replace(" ", "_").replace("%20", "_");// 文件原名称
            LOG.info("上传的文件原名称:" + fileName);
            // 判断文件类型
            String type = fileName.indexOf(".") != -1
                    ? fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
                    : null;
            String Path =File.separator+"data";
            if (System.getProperties().getProperty("os.name").contains("Windows")){
                String userHome = System.getProperties().getProperty("user.home");
                Path=userHome+Path;
            }
            String realPath = StringUtil.merge(Path, dir.replace("/",File.separator));
            // 创建目录
            File folder = new File(realPath);
            if (!folder.isDirectory())
                folder.mkdirs();
            // 自定义的文件名称
            String[] split = givenName.split(" ");
            StringBuffer mgivenName = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
                char charAt = split[i].charAt(0);
                mgivenName.append(charAt);
            }
            String newFileName =name + "_" + rFamilyName + mgivenName + "_" + formatdate ;
            // 设置存放文件的路径
            String path = StringUtil.merge(realPath, newFileName,".", type);
            LOG.info("存放文件的路径:" + path);
            // 转存文件到指定的路径
            file.transferTo(new File(path));
            return StringUtil.merge(dir, newFileName,".", type);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public void deleteFile(String url) throws ServiceException {
        if (url == null){
            ServiceException se = new ServiceException("删除路径为空!");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            String realpath=StringUtil.merge("/data",url);
            Files.delete(Paths.get(realpath));
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public CustomerInformationDO getByApplicantId(int applicantId) throws ServiceException {
        try {
            return customerInformationDAO.getByApplicantId(applicantId);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }


    public void sendRemind(int id) {
        ServiceOrderDO serviceOrderDo = serviceOrderDAO.getServiceOrderById(id);
        AdviserDO adviser = adviserDao.getAdviserById(serviceOrderDo.getAdviserId());
        OfficialDO official = officialDAO.getOfficialById(serviceOrderDo.getOfficialId());
        String t = serviceOrderDo.getType();
        String type = getType(t);

        ApplicantDTO applicantDto = null;
        if (serviceOrderDo.getApplicantId() > 0)
            applicantDto = mapper.map(applicantDao.getById(serviceOrderDo.getApplicantId()), ApplicantDTO.class);
        applicantDto = buildApplicant(applicantDto, serviceOrderDo.getId(), serviceOrderDo.getNutCloud(),
                serviceOrderDo.getInformation());
        ServiceDO service = serviceDao.getServiceById(serviceOrderDo.getServiceId());

        sendMail(official.getEmail(), "你有一条新的客户资料更新请及时处理。", StringUtil.merge("订单号:", id, "<br/>", "服务类型:", type,
                "/申请人名称:",
                ObjectUtil.isNotNull(applicantDto) ? applicantDto.getFirstname() + " " + applicantDto.getSurname()
                        : "unknown",
                "/类型:",
                ObjectUtil.isNotNull(applicantDto) && ObjectUtil.isNotNull(service)
                        ? service.getName() + "(" + service.getCode() + ")"
                        : "unknown",
                "/顾问:", adviserDao.getAdviserById(serviceOrderDo.getAdviserId()).getName(), "/文案:",
                officialDAO.getOfficialById(serviceOrderDo.getOfficialId()).getName(), "<br/>", "属性:",
                getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>客户的资料地址: https://yongjinbiao.zhinanzhen.org/webroot_new/attachments/serviceOrder/get?id=", serviceOrderDo.getId(),
                "<br/>在线资料地址:", applicantDto.getUrl(), "<br/>客户基本信息:", applicantDto.getContent(), "<br/>备注:",
                serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:",
                serviceOrderDo.getGmtCreate(),
                "<br/><br/><a href='https://yongjinbiao.zhinanzhen.org/webroot_new/serviceorderdetail/id?" + id
                        + "'>服务订单详情</a>")
        );

        if (ObjectUtil.isNotNull(applicantDto)) {
            sendMail(adviser.getEmail(),
                    StringUtil.merge("申请人", applicantDto.getFirstname(), " ", applicantDto.getSurname(),
                            "完成资料postal提醒"),
                    StringUtil.merge("亲爱的", adviser.getName(), ":<br/>", "您的服务订单ID", id, ",申请人:",
                            applicantDto.getFirstname(), " ", applicantDto.getSurname(),
                            " 资料已经填写完毕,请在服务订单-查看-申请人Tab 内查看．"));
            sendMail(official.getEmail(),
                    StringUtil.merge("申请人", applicantDto.getFirstname(), " ", applicantDto.getSurname(),
                            "完成资料postal提醒"),
                    StringUtil.merge("亲爱的", official.getName(), ":<br/>", "您的服务订单ID", id, ",申请人:",
                            applicantDto.getFirstname(), " ", applicantDto.getSurname(),
                            " 资料已经填写完毕,请在服务订单-查看-申请人Tab 内查看．"));
        }
    }

    private String getPeopleTypeStr(String peopleType) {
        if ("1A".equalsIgnoreCase(peopleType))
            return "单人";
        else if ("1B".equalsIgnoreCase(peopleType))
            return "单人提配偶";
        else if ("2A".equalsIgnoreCase(peopleType))
            return "带配偶";
        else if ("XA".equalsIgnoreCase(peopleType))
            return "带孩子";
        else if ("XB".equalsIgnoreCase(peopleType))
            return "带配偶孩子";
        else if ("XC".equalsIgnoreCase(peopleType))
            return "其它";
        else
            return "未知";
    }

    private ApplicantDTO buildApplicant(ApplicantDTO applicantDto, Integer serviceOrderId, String notCloud,
                                        String information) {
        if (applicantDto == null)
            return applicantDto;
        List<ServiceOrderApplicantDO> serviceOrderApplicantDoList = serviceOrderApplicantDao.list(serviceOrderId,
                applicantDto.getId());
        if (serviceOrderApplicantDoList != null && serviceOrderApplicantDoList.size() > 0
                && serviceOrderApplicantDoList.get(0) != null) {
            applicantDto.setUrl(serviceOrderApplicantDoList.get(0).getUrl());
            applicantDto.setContent(serviceOrderApplicantDoList.get(0).getContent());
        }
        if (StringUtil.isEmpty(applicantDto.getUrl()))
            applicantDto.setUrl(notCloud);
        if (StringUtil.isEmpty(applicantDto.getContent()))
            applicantDto.setContent(information);
        return applicantDto;
    }

    private String getType(String type) {
        String s = "";
        if ("VISA".equalsIgnoreCase(s)) {
            s = "签证";
        } else if ("OVST".equalsIgnoreCase(type)) {
            s = "留学";
        } else if ("SIV".equalsIgnoreCase(type)) {
            s = "独立技术移民";
        } else if ("NSV".equalsIgnoreCase(type)) {
            s = "雇主担保";
        } else if ("MT".equalsIgnoreCase(type)) {
            s = "曼拓";
        } else if ("ZX".equalsIgnoreCase(type)) {
            s = "咨询";
        }
        return s;
    }
    private Map<String, Object> webdav(CustomerInformationDO customerInformationDO) throws IOException, ServiceException {
        if(customerInformationDO.getMainInformation().getFamilyName().contains(" ")){
            ServiceException se = new ServiceException("上传失败！客户姓有空格符，请修改后重新上传!");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            String givenName = customerInformationDO.getMainInformation().getGivenName();
            String familyName = customerInformationDO.getMainInformation().getFamilyName();
            String rgivenName = givenName.replace(" ", "");
            String[] split = givenName.split(" ");
            StringBuffer mgivenName = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
                char charAt = split[i].charAt(0);
                mgivenName.append(charAt);
            }
            LocalDate date = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            String formatdate = date.format(formatter);
            String folderName = familyName + mgivenName + "_" + formatdate;

            // 使用阿里云PDS上传文件，driveId=10810
            String driveId = "10810";
            AsyncClient client = getAliyunPdsClient();

            // 1. 创建文件夹
            CreateFileRequest createFolderRequest = CreateFileRequest.builder()
                    .name(folderName)
                    .type("folder")
                    .parentFileId("root")
                    .driveId(driveId)
                    .build();
            CompletableFuture<CreateFileResponse> folderResponse = client.createFile(createFolderRequest);
            CreateFileResponse folderResp = folderResponse.get();
            String folderJson = new Gson().toJson(folderResp);
            JSONObject folderJsonObject = JSON.parseObject(folderJson);
            JSONObject folderBody = folderJsonObject.getJSONObject("body");
            String folderFileId = folderBody.get("fileId").toString();

            // 2. 获取文件列表并逐个上传
            List<String> list = getUrlList(customerInformationDO);
            List<CloudDiskFile> cloudDiskFiles = new ArrayList<>();
            if (list != null && !list.isEmpty()) {
                for (String filePath : list) {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        continue;
                    }
                    String fileName = file.getName();
                    long fileSize = file.length();

                    // 创建文件元信息，获取上传URL
                    CreateFileRequest.ParallelSha1Ctx partInfoParallelSha1Ctx = CreateFileRequest.ParallelSha1Ctx.builder()
                            .partOffset(fileSize)
                            .build();
                    CreateFileRequest.PartInfoList partInfo = CreateFileRequest.PartInfoList.builder()
                            .partNumber(1)
                            .parallelSha1Ctx(partInfoParallelSha1Ctx)
                            .build();

                    CreateFileRequest createFileRequest = CreateFileRequest.builder()
                            .name(fileName)
                            .type("file")
                            .parentFileId(folderFileId)
                            .driveId(driveId)
                            .size(fileSize)
                            .partInfoList(java.util.Arrays.asList(partInfo))
                            .build();
                    CompletableFuture<CreateFileResponse> fileResponse = client.createFile(createFileRequest);
                    CreateFileResponse fileResp = fileResponse.get();
                    String fileJson = new Gson().toJson(fileResp);
                    JSONObject fileJsonObject = JSON.parseObject(fileJson);
                    JSONObject fileBody = fileJsonObject.getJSONObject("body");
                    String fileId = fileBody.get("fileId").toString();
                    String uploadId = fileBody.get("uploadId").toString();

                    // 上传文件内容
                    String partInfoJsonString = JSONObject.toJSONString(fileBody);
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    UploadResponseData responseData = objectMapper.readValue(partInfoJsonString, UploadResponseData.class);
                    List<PartInfo> partInfoList = responseData.getPartInfoList();

                    for (PartInfo uploadPartInfo : partInfoList) {
                        int number = uploadPartInfo.getPartNumber();
                        long pos = (number - 1) * fileSize;
                        long size = fileSize;
                        byte[] partContent = new byte[(int) size];

                        java.io.RandomAccessFile randomAccessFile = new java.io.RandomAccessFile(file, "r");
                        randomAccessFile.seek(pos);
                        randomAccessFile.readFully(partContent, 0, (int) size);
                        randomAccessFile.close();

                        RequestBody body = RequestBody.create(null, partContent);
                        Request uploadRequest = new Request.Builder()
                                .url(uploadPartInfo.getUploadUrl())
                                .header("Content-Length", String.valueOf(size))
                                .put(body)
                                .build();

                        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                        okhttp3.Response uploadResponse = okHttpClient.newCall(uploadRequest).execute();
                        if (!uploadResponse.isSuccessful()) {
                            throw new RuntimeException("上传文件分片失败, partNumber:" + number);
                        }
                    }

                    // 完成文件上传
                    CompleteFileRequest completeFileRequest = CompleteFileRequest.builder()
                            .driveId(driveId)
                            .fileId(fileId)
                            .uploadId(uploadId)
                            .build();
                    CompletableFuture<CompleteFileResponse> completeResponse = client.completeFile(completeFileRequest);
                    CompleteFileResponse completeResp = completeResponse.get();

                    // 记录文件信息
                    CloudDiskFile cloudDiskFile = CloudDiskFile.builder()
                            .fileId(fileId)
                            .name(fileName)
                            .type("file")
                            .parentFileId(folderFileId)
                            .driveId(driveId)
                            .domainId(aliyunPdsDomainId)
                            .fileSize(fileSize)
                            .relativePath("/root/" + folderName + "/" + fileName)
                            .build();
                    cloudDiskFiles.add(cloudDiskFile);
                }
            }

            client.close();

            // 构建返回结果：文件夹fileId + 文件信息列表
            Map<String, Object> result = new HashMap<>();
            result.put("folderFileId", folderFileId);
            result.put("cloudDiskFiles", cloudDiskFiles);
            return result;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    /**
     * 获取阿里云PDS客户端
     */
    private AsyncClient getAliyunPdsClient() {
        StaticCredentialProvider provider = StaticCredentialProvider.create(com.aliyun.auth.credentials.Credential.builder()
                .accessKeyId(aliyunAccessKeyId)
                .accessKeySecret(aliyunAccessKeySecret)
                .build());

        return AsyncClient.builder()
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride(aliyunPdsEndpoint)
                )
                .build();
    }
    /**
     * 获取文件夹下所有文件的路径
     *
     * @param folderPath
     * @return
     */
    public static List<String> getFilePath(String folderPath) {
        File folder = new File(folderPath);
        List<String> filePathList = new ArrayList<>();
        String rootPath;
        if (folder.exists()) {
            String[] fileNameList = folder.list();
            if (null != fileNameList && fileNameList.length > 0) {
                if (folder.getPath().endsWith(File.separator)) {
                    rootPath = folder.getPath();
                } else {
                    rootPath = folder.getPath() + File.separator;
                }
                for (String fileName : fileNameList) {
                    filePathList.add(rootPath + fileName);
                }
            }
        }
        return filePathList;
    }

    public  void deleteAll(CustomerInformationDO customerInformationDO) throws ServiceException {
        String familyName = customerInformationDO.getMainInformation().getFamilyName();
        String givenName = customerInformationDO.getMainInformation().getGivenName();
        String rFamilyName = familyName.replace(" ", "");
        String rgivenName = givenName.replace(" ", "");
        LocalDate date = LocalDate.now(); // get the current date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        String formatdate = date.format(formatter);
        String dir =File.separator+"data";
        if (System.getProperties().getProperty("os.name").contains("Windows")){
            String userHome = System.getProperties().getProperty("user.home");
            dir=userHome+dir;
        }
        String fileDir = "/uploads/customerInformation/"+rFamilyName.toUpperCase() +"_"+ rgivenName.toUpperCase() + "/";
        List<String> filePath = getFilePath(dir + fileDir);
        for (String url : filePath) {
            if (url == null){
                ServiceException se = new ServiceException("删除路径为空!");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
            try {
                Files.delete(Paths.get(url));
            } catch (Exception e) {
                ServiceException se = new ServiceException(e);
                se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
                throw se;
            }
        }

    }

    public List<String> getUrlList(CustomerInformationDO customerInformationDO) {
        List<Object> objectList = new ArrayList<>();
        objectList.add(customerInformationDO.getUrl().getBirth());
        objectList.add(customerInformationDO.getUrl().getPassport());
        objectList.add(customerInformationDO.getUrl().getPhotoId());
        if(ObjectUtil.isNotNull(customerInformationDO.getUrl().getTpassport())){
            objectList.add(customerInformationDO.getUrl().getTpassport());
        }
        if(ObjectUtil.isNotNull(customerInformationDO.getUrl().getOther())){
            objectList.add(customerInformationDO.getUrl().getOther());
        }
        List<String > list = new ArrayList<>();
        String dir =File.separator+"data";
        if (System.getProperties().getProperty("os.name").contains("Windows")){
            String userHome = System.getProperties().getProperty("user.home");
            dir=userHome+dir;
        }

        for (Object object : objectList) {
            if (null == object) {
                return null;
            }
            try {
                // 挨个获取对象属性值
                for (Field f : object.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    // 如果有一个属性值不为null，且值不是空字符串，就返回false
                    if (f.get(object) != null && StringUtils.isNotBlank(f.get(object).toString())) {
                        String replace = f.get(object).toString().replace("/", File.separator);
                        String s = StringUtil.merge(dir, replace);
                        list.add(s);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    //坚果云下载
    @Override
    public CustomerInformationDO getFileByDav(int applicantId) throws ServiceException {

        try{
            CustomerInformationDO customerInformationDO = customerInformationDAO.getByApplicantId(applicantId);
            if (ObjectUtil.isNotNull(customerInformationDO)&&ObjectUtil.isNotNull(customerInformationDO.getUrl())){
                String givenName = customerInformationDO.getMainInformation().getGivenName();
                String familyName = customerInformationDO.getMainInformation().getFamilyName();
                String rFamilyName = familyName.replace(" ", "");
                String rgivenName = givenName.replace(" ", "");
                String mmdiskPath = customerInformationDO.getMmdiskPath().replace("\"","");
                String outpath = "/uploads/customerInformation/"+rFamilyName.toUpperCase() +"_"+ rgivenName.toUpperCase()+"/" ;
                String dir =File.separator+"data";
                if (System.getProperties().getProperty("os.name").contains("Windows")){
                    String userHome = System.getProperties().getProperty("user.home");
                    dir=userHome+dir;
                }
                outpath=dir+outpath;
                List<String> urlList = WebDavUtils.MMdown(mmdiskPath,outpath);
                //return urlList;
            }
            if (ObjectUtil.isNotNull(customerInformationDO)&&customerInformationDO.getMmdiskPath() != null) {
                String mmdiskPath = customerInformationDO.getMmdiskPath().replace("\"", "");
                customerInformationDO.setMmdiskPath(mmdiskPath);
                return customerInformationDO;
            }

            return null;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }

    }

    // 阿里云识别
//    @Override
//    public IdentifyingInformationDO identifyingInformation(String familyName, String givenName, String name, MultipartFile file) throws ServiceException, IOException {
//        if (file == null) {
//            ServiceException se = new ServiceException("上传文件为空!");
//            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
//            throw se;
//        }
//        try {
//            IdentifyingInformationDO identifyingInformationDO = new IdentifyingInformationDO();
//            byte[] bytes = file.getBytes();
//            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
//                    // 必填，您的 AccessKey ID
//                    .setAccessKeyId(alibabaSecretId)
//                    // 必填，您的 AccessKey Secret
//                    .setAccessKeySecret(alibabaSecretKey);
//            // Endpoint 请参考 https://api.aliyun.com/product/ocr-api
//            config.endpoint = "ocr-api.cn-hangzhou.aliyuncs.com";
//            com.aliyun.ocr_api20210707.Client client = new com.aliyun.ocr_api20210707.Client(config);
//            InputStream bodyStream = Client.readFromBytes(bytes);
//            com.aliyun.ocr_api20210707.models.RecognizeChinesePassportRequest recognizeChinesePassportRequest = new com.aliyun.ocr_api20210707.models.RecognizeChinesePassportRequest()
//                    .setBody(bodyStream);
//            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
//            com.aliyun.ocr_api20210707.models.RecognizeChinesePassportResponse resp = client.recognizeChinesePassportWithOptions(recognizeChinesePassportRequest, runtime);
//            String data = resp.getBody().getData();
//            JSONObject jsonObject = JSONObject.parseObject(data);
//            JSONObject dataTmp = JSONObject.parseObject(jsonObject.getString("data"));
//            // 英文姓名
//            String nameEn = dataTmp.getString("nameEn");
//            String[] split = nameEn.split(",");
//            // 英文姓
//            identifyingInformationDO.setFamilyName(split[0]);
//            // 英文名
//            identifyingInformationDO.setGivenName(split[1]);
//            // 护照号码
//            identifyingInformationDO.setPassportNumber(dataTmp.getString("passportNumber"));
//            // 性别
//            identifyingInformationDO.setGender(dataTmp.getString("sex").split("/")[1]);
//            // 签发地点
//            identifyingInformationDO.setIssuePlace(dataTmp.getString("issuePlace").split("/")[1]);
//            // 出生地
//            identifyingInformationDO.setBirthLocation(dataTmp.getString("birthPlace").split("/")[1]);
//            // 出生国家
//            identifyingInformationDO.setBirthCountry(dataTmp.getString("countryCode"));
//            // 省份
//            identifyingInformationDO.setStateOrProvince(dataTmp.getString("birthPlace").split("/")[1]);
//            // 生日
//            String birthDate = dataTmp.getString("birthDate");
//            if (StringUtils.isNotBlank(birthDate)) {
//                String[] birthDateSplit = birthDate.split("\\.");
//                identifyingInformationDO.setDateOfBirth(birthDateSplit[2] + "/" + birthDateSplit[1] + "/" + birthDateSplit[0]);
//            }
//            // 签发日期
//            String issueDate = dataTmp.getString("issueDate");
//            if (StringUtils.isNotBlank(issueDate)) {
//                String[] issueDateSplit = issueDate.split("\\.");
//                identifyingInformationDO.setIssueDate(issueDateSplit[2] + "/" + issueDateSplit[1] + "/" + issueDateSplit[0]);
//            }
//            // 到期日期
//            String validToDate = dataTmp.getString("validToDate");
//            if (StringUtils.isNotBlank(validToDate)) {
//                String[] validToDateSplit = validToDate.split("\\.");
//                identifyingInformationDO.setExpiryDate(validToDateSplit[2] + "/" + validToDateSplit[1] + "/" + validToDateSplit[0]);
//            }
//            String uploadUrl = this.upload(familyName, givenName, name, file);
//            identifyingInformationDO.setUrl(uploadUrl);
//            return identifyingInformationDO;
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//    }

    // 腾讯云识别
    @Override
    public IdentifyingInformationDO identifyingInformation(String familyName, String givenName, String name, MultipartFile file) throws ServiceException, IOException {
        if (file == null) {
            ServiceException se = new ServiceException("上传文件为空!");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        byte[] bytes = file.getBytes();
        try {
            String imageBase = Base64Util.encodeBase64(bytes);
            String secretIdDecrypt = AESUtils.decrypt(AESUtils.loadKeyAES(secretId), "SWxLBbK0i0tfFfXXw10Hrh6I3OaOcYWHNFVj1ohDHjnh92r4xEdKYfkOU+1/LEC0", "UTF-8");
            String secretKeyDecrypt = AESUtils.decrypt(AESUtils.loadKeyAES(secretKey), "QX+o8uK3s2K+RYs5Rzf84wgHShAPXUAtMlmLzejPWa9KP6CSmXeZFK0h19tBAWdD", "UTF-8");
            Credential credential = new Credential(secretIdDecrypt, secretKeyDecrypt);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ocr.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            OcrClient client = new OcrClient(credential, "ap-beijing", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            PassportOCRRequest req = new PassportOCRRequest();
            req.setImageBase64(imageBase);
            // 返回的resp是一个PassportOCRResponse的实例，与请求对象对应
            PassportOCRResponse resp = client.PassportOCR(req);
            // 输出json格式的字符串回包
            String s1 = PassportOCRResponse.toJsonString(resp);
            JSONObject jsonObject = JSONObject.parseObject(s1);
            IdentifyingInformationDO identifyingInformationDO1 = new IdentifyingInformationDO();
            // 护照号码
            identifyingInformationDO1.setPassportNumber(jsonObject.getString("PassportNo"));
            // 性别
            identifyingInformationDO1.setGender(jsonObject.getString("Sex"));
            // 名字拼音
            identifyingInformationDO1.setGivenName(jsonObject.getString("FirstName"));
            // 姓拼音
            identifyingInformationDO1.setFamilyName(jsonObject.getString("FamilyName"));
            // 生日
            String birthDate = jsonObject.getString("BirthDate");
            if (StringUtils.isNotBlank(birthDate)) {
                String year = birthDate.substring(0, 4);
                String month = birthDate.substring(4, 6);
                String day = birthDate.substring(6, 8);
                String birthTime = day + "/" + month + "/" + year;
                identifyingInformationDO1.setDateOfBirth(birthTime);
            }
            // 签发日期
            String issueDate = jsonObject.getString("IssueDate");
            if (StringUtils.isNotBlank(issueDate)) {
                String issueYear = issueDate.substring(0, 4);
                String issueMonth = issueDate.substring(4, 6);
                String issueDay = issueDate.substring(6, 8);
                String issueTime = issueDay + "/" + issueMonth + "/" + issueYear;
                identifyingInformationDO1.setIssueDate(issueTime);
            }
            // 有效期
            String expiryDate = jsonObject.getString("ExpiryDate");
            if (StringUtils.isNotBlank(expiryDate)) {
                String expiryYear = expiryDate.substring(0, 4);
                String expiryMonth = expiryDate.substring(4, 6);
                String expiryDay = expiryDate.substring(6, 8);
                String expiryTime = expiryDay + "/" + expiryMonth + "/" + expiryYear;
                identifyingInformationDO1.setExpiryDate(expiryTime);
            }
            // 签发地点
            identifyingInformationDO1.setIssuePlace(jsonObject.getString("IssuePlace"));
            // 出生地
            identifyingInformationDO1.setBirthLocation(jsonObject.getString("BirthPlace"));
            // 出生国家
            identifyingInformationDO1.setBirthCountry(jsonObject.getString("Nationality"));
            // 省份
            identifyingInformationDO1.setStateOrProvince(jsonObject.getString("BirthPlace"));
            String uploadUrl = this.upload(familyName, givenName, name, file);
            identifyingInformationDO1.setUrl(uploadUrl);
            return identifyingInformationDO1;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
