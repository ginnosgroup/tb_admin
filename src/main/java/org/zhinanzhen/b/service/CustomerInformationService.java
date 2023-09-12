package org.zhinanzhen.b.service;

import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.b.dao.pojo.IdentifyingInformationDO;
import org.zhinanzhen.tb.service.ServiceException;

import java.io.IOException;

public interface CustomerInformationService {

    void add(CustomerInformationDO customerInformationDO) throws ServiceException;

    CustomerInformationDO get(int id)throws ServiceException;

    void update(CustomerInformationDO record)throws ServiceException;

    void delete(int id)throws ServiceException;

    CustomerInformationDO getByServiceOrderId(int serviceOrderId)throws ServiceException;

    CustomerInformationDO getByApplicantId(int applicantId)throws ServiceException;

    String upload(String familyName,String givenName,String name, MultipartFile file)throws ServiceException, IOException;

    void deleteFile(String url) throws ServiceException;

    //坚果云下载
    CustomerInformationDO getFileByDav(int applicantId) throws ServiceException;

    // 识别护照并返回个人信息
    IdentifyingInformationDO identifyingInformation(String familyName,String givenName,String name, MultipartFile file) throws ServiceException, IOException;;

}
