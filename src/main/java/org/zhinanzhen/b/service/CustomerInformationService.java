package org.zhinanzhen.b.service;

import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.tb.service.ServiceException;

import java.io.IOException;

public interface CustomerInformationService {

    void add(CustomerInformationDO customerInformationDO) throws ServiceException;

    CustomerInformationDO get(int id)throws ServiceException;

    void update(CustomerInformationDO record)throws ServiceException;

    void delete(int id)throws ServiceException;

    CustomerInformationDO getByServiceOrderId(int serviceOrderId)throws ServiceException;

    CustomerInformationDO getByApplicantId(int applicantId)throws ServiceException;

    String upload(int serviceOrderId,String name, MultipartFile file)throws ServiceException, IOException;

}
