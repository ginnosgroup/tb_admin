package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.OrderMaraAnnotationDAO;
import org.zhinanzhen.b.dao.pojo.OrderMaraAnnotationDO;
import org.zhinanzhen.b.service.OrderMaraAnnotationService;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("OrderMaraAnnotationService")
public class OrderMaraAnnotationServiceImpl implements OrderMaraAnnotationService {

    @Resource
    private OrderMaraAnnotationDAO orderMaraAnnotationDao;

    @Override
    public int saveMaraMark(int serviceOrderId, String maraMark) throws ServiceException {
        if (StringUtil.isEmpty(maraMark)) {
            return 0;
        }
        try {
            // 一个 serviceOrderId 只有一条，有则更新，无则新增
            OrderMaraAnnotationDO exist = orderMaraAnnotationDao.getByServiceOrderId(serviceOrderId);
            if (exist != null) {
                exist.setMaraMark(maraMark);
                return orderMaraAnnotationDao.update(exist);
            } else {
                OrderMaraAnnotationDO newDo = new OrderMaraAnnotationDO();
                newDo.setServiceOrderId(serviceOrderId);
                newDo.setMaraMark(maraMark);
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
        try {
            OrderMaraAnnotationDO result = orderMaraAnnotationDao.getByServiceOrderId(serviceOrderId);
            return result != null ? result.getMaraMark() : null;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

}
