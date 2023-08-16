package org.zhinanzhen.b.service.impl;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ServiceDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderApplicantDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.b.service.CustomerInformationService;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.tb.utils.WebDavUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void add(CustomerInformationDO customerInformationDO) throws ServiceException {
        if(customerInformationDO.getMainInformation().getFamilyName().contains(" ")){
            ServiceException se = new ServiceException("保存失败！客户姓有空格符，请修改后重新保存!");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            String webdav = webdav(customerInformationDO);
            customerInformationDO.setMmdiskPath(webdav);
            customerInformationDAO.insert(customerInformationDO);
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
            CustomerInformationDO customerInformationDO=null;
             customerInformationDO = customerInformationDAO.getByServiceOrderId(id);
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
            return StringUtil.merge(StringUtil.merge("/statics", dir), newFileName,".", type);
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
				getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:", applicantDto.getUrl(),
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
    private String webdav(CustomerInformationDO customerInformationDO) throws IOException, ServiceException {
        if(customerInformationDO.getMainInformation().getFamilyName().contains(" ")){
            ServiceException se = new ServiceException("上传失败！客户姓有空格符，请修改后重新上传!");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }try {
            String givenName = customerInformationDO.getMainInformation().getGivenName();
            String familyName = customerInformationDO.getMainInformation().getFamilyName();
            String rgivenName = givenName.replace(" ", "");
            String[] split = givenName.split(" ");
            StringBuffer mgivenName = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
                char charAt = split[i].charAt(0);
                mgivenName.append(charAt);
            }
            LocalDate date = LocalDate.now(); // get the current date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            String formatdate = date.format(formatter);
            String netDiskPath = "https://dav.jianguoyun.com/dav/MMtest/" + familyName  + mgivenName + "_" + formatdate ;
            //String filePath = "C:/Users/yjt/Desktop/data/uploads/customerInformation/" + familyName +"_"+ rgivenName  ;
//            String testnetDiskPath="https://dav.jianguoyun.com/dav/MMtest/MAK_21072023/afp01_MAK_21072023.jpg";
//            String testfilePath="/data/uploads/customerInformation/MA_KE/afp01_MAK_21072023.jpg";
//            List<String> path = getFilePath(filePath);
//            if (path.size()==0){
//                ServiceException se = new ServiceException("文件未上传成功，请重新上传"+filePath);
//                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
//                throw se;
//            }
            List<String> list = getUrlList(customerInformationDO);
            WebDavUtils.upload2(netDiskPath,list);
            String mmdir = netDiskPath.substring(netDiskPath.lastIndexOf("/") + 1);
            return mmdir;
            //文件上传

            //WebDavUtils.upload2(netDiskPath, path);
        }catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }

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







}
