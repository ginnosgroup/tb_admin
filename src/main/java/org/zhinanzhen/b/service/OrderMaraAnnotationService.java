package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.OrderMaraAnnotationDO;
import org.zhinanzhen.tb.service.ServiceException;

public interface OrderMaraAnnotationService {

    int saveMaraMark(int serviceOrderId, String maraMark) throws ServiceException;

    int saveMaraMark(int serviceOrderId, String maraMark, boolean isCheck) throws ServiceException;

    int saveMaraMark(int serviceOrderId, String maraMark, boolean isCheck, int officialId) throws ServiceException;

    String getMaraMarkByServiceOrderId(int serviceOrderId) throws ServiceException;

    OrderMaraAnnotationDO getByServiceOrderId(int serviceOrderId) throws ServiceException;

    int officialCheck(int serviceOrderId, int officialId) throws ServiceException;

}
