package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.OrderMaraAnnotationDO;
import org.zhinanzhen.b.service.pojo.SelectOfficialCheckDTO;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

public interface OrderMaraAnnotationService {

    int saveMaraMark(int serviceOrderId, String maraMark) throws ServiceException;

    int saveMaraMark(int serviceOrderId, String maraMark, boolean isCheck) throws ServiceException;

    int saveMaraMark(int serviceOrderId, String maraMark, boolean isCheck, int officialId) throws ServiceException;

    int saveMaraMarkFromServiceOrder(int serviceOrderId, String maraMark) throws ServiceException;

    String getMaraMarkByServiceOrderId(int serviceOrderId) throws ServiceException;

    OrderMaraAnnotationDO getByServiceOrderId(int serviceOrderId) throws ServiceException;

    int officialCheck(int serviceOrderId, int officialId) throws ServiceException;

    List<OrderMaraAnnotationDO> listByOfficialId(int officialId) throws ServiceException;

    List<SelectOfficialCheckDTO> selectOfficialCheck(int officialId) throws ServiceException;

}
