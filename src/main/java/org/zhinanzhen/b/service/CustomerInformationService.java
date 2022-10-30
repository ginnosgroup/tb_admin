package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.tb.service.ServiceException;

public interface CustomerInformationService {

    void add(CustomerInformationDO customerInformationDO) throws ServiceException;

    CustomerInformationDO get(int id)throws ServiceException;

    void update(CustomerInformationDO record)throws ServiceException;

    void delete(int id)throws ServiceException;
}