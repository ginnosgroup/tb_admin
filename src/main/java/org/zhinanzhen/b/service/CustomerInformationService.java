package org.zhinanzhen.b.service;

import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.tb.service.ServiceException;

import java.io.IOException;
import java.util.List;

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
    List<String> getFileByDav(int applicantId) throws ServiceException;
}
