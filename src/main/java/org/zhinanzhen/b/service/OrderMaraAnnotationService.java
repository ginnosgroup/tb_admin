package org.zhinanzhen.b.service;

import org.zhinanzhen.tb.service.ServiceException;

public interface OrderMaraAnnotationService {

    int saveMaraMark(int serviceOrderId, String maraMark) throws ServiceException;

    String getMaraMarkByServiceOrderId(int serviceOrderId) throws ServiceException;

    int officialCheck(int serviceOrderId, int officialId) throws ServiceException;

}
