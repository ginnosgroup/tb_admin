package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.OrderMaraAnnotationDAO;
import org.zhinanzhen.b.dao.pojo.OrderMaraAnnotationDO;
import org.zhinanzhen.b.service.OrderMaraAnnotationService;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.ErrorCodeEnum;

@Service("OrderMaraAnnotationService")
public class OrderMaraAnnotationServiceImpl implements OrderMaraAnnotationService {

    @Resource
    private OrderMaraAnnotationDAO orderMaraAnnotationDao;

    @Override
    public int saveMaraMark(int serviceOrderId, String maraMark) throws ServiceException {
        return saveMaraMark(serviceOrderId, maraMark, false);
    }

    @Override
    public int saveMaraMark(int serviceOrderId, String maraMark, boolean isCheck) throws ServiceException {
        return saveMaraMark(serviceOrderId, maraMark, isCheck, 0);
    }

    @Override
    public int saveMaraMark(int serviceOrderId, String maraMark, boolean isCheck, int officialId) throws ServiceException {
        try {
            OrderMaraAnnotationDO exist = orderMaraAnnotationDao.getByServiceOrderId(serviceOrderId);
            if (exist != null) {
                exist.setMaraMark(maraMark);
                if (isCheck) {
                    exist.setIsCheck(true);
                    exist.setCheckTime(new Date());
                    if (officialId > 0) {
                        exist.setOfficialId(officialId);
                    }
                }
                return orderMaraAnnotationDao.update(exist);
            } else {
                OrderMaraAnnotationDO newDo = new OrderMaraAnnotationDO();
                newDo.setServiceOrderId(serviceOrderId);
                newDo.setMaraMark(maraMark);
                newDo.setIsCheck(isCheck);
                if (isCheck) {
                    newDo.setCheckTime(new Date());
                    if (officialId > 0) {
                        newDo.setOfficialId(officialId);
                    }
                }
                return orderMaraAnnotationDao.add(newDo);
            }
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public String getMaraMarkByServiceOrderId(int serviceOrderId) throws ServiceException {
        OrderMaraAnnotationDO result = getByServiceOrderId(serviceOrderId);
        return result != null ? result.getMaraMark() : null;
    }

    @Override
    public OrderMaraAnnotationDO getByServiceOrderId(int serviceOrderId) throws ServiceException {
        try {
            return orderMaraAnnotationDao.getByServiceOrderId(serviceOrderId);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public int officialCheck(int serviceOrderId, int officialId) throws ServiceException {
        try {
            OrderMaraAnnotationDO exist = orderMaraAnnotationDao.getByServiceOrderId(serviceOrderId);
            if (exist != null) {
                exist.setIsCheck(true);
                exist.setCheckTime(new Date());
                exist.setOfficialId(officialId);
                return orderMaraAnnotationDao.update(exist);
            } else {
                OrderMaraAnnotationDO newDo = new OrderMaraAnnotationDO();
                newDo.setServiceOrderId(serviceOrderId);
                newDo.setIsCheck(true);
                newDo.setCheckTime(new Date());
                newDo.setOfficialId(officialId);
                return orderMaraAnnotationDao.add(newDo);
            }
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

}
