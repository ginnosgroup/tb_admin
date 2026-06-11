package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.OrderMaraAnnotationDAO;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.pojo.OrderMaraAnnotationDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.FileMaraAnnotationService;
import org.zhinanzhen.b.service.OrderMaraAnnotationService;
import org.zhinanzhen.b.service.pojo.SelectOfficialCheckDTO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.ErrorCodeEnum;

@Service("OrderMaraAnnotationService")
public class OrderMaraAnnotationServiceImpl implements OrderMaraAnnotationService {

    @Resource
    private OrderMaraAnnotationDAO orderMaraAnnotationDao;

    @Resource
    private ServiceOrderDAO serviceOrderDao;

    @Resource
    private FileMaraAnnotationService fileMaraAnnotationService;

    @Resource
    private UserDAO userDao;

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
                if (maraMark != null) {
                    exist.setMaraMark(maraMark);
                }
                exist.setIsCheck(isCheck);
                if (officialId > 0) {
                    exist.setOfficialId(officialId);
                }
                if (isCheck) {
                    exist.setCheckTime(new Date());
                } else {
                    exist.setCheckTime(null);
                }
                return orderMaraAnnotationDao.update(exist);
            } else {
                OrderMaraAnnotationDO newDo = new OrderMaraAnnotationDO();
                newDo.setServiceOrderId(serviceOrderId);
                newDo.setMaraMark(maraMark);
                newDo.setIsCheck(isCheck);
                if (officialId > 0) {
                    newDo.setOfficialId(officialId);
                }
                if (isCheck) {
                    newDo.setCheckTime(new Date());
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

    @Override
    public int saveMaraMarkFromServiceOrder(int serviceOrderId, String maraMark) throws ServiceException {
        int officialId = 0;
        try {
            ServiceOrderDO so = serviceOrderDao.getServiceOrderById(serviceOrderId);
            if (so != null) {
                officialId = so.getOfficialId();
            }
        } catch (Exception e) {
            // ignore, officialId stays 0
        }
        return saveMaraMark(serviceOrderId, maraMark, false, officialId);
    }

    @Override
    public List<OrderMaraAnnotationDO> listByOfficialId(int officialId) throws ServiceException {
        try {
            return orderMaraAnnotationDao.listByOfficialId(officialId);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public List<SelectOfficialCheckDTO> selectOfficialCheck(int officialId) throws ServiceException {
        try {
            List<OrderMaraAnnotationDO> omList = orderMaraAnnotationDao.listByOfficialId(officialId);
            if (omList == null) {
                return new ArrayList<>();
            }
            List<SelectOfficialCheckDTO> resultList = new ArrayList<>();
            for (OrderMaraAnnotationDO om : omList) {
                SelectOfficialCheckDTO dto = new SelectOfficialCheckDTO();
                dto.setOrderMaraAnnotation(om);
                if (om.getServiceOrderId() > 0) {
                    ServiceOrderDO so = serviceOrderDao.getServiceOrderById(om.getServiceOrderId());
                    if (so != null && so.getUserId() > 0) {
                        so.setUserDO(userDao.getUserById(so.getUserId()));
                    }
                    dto.setServiceOrder(so);
                    dto.setFileMaraAnnotationList(fileMaraAnnotationService.listSimple(om.getServiceOrderId(), null, officialId));
                }
                resultList.add(dto);
            }
            return resultList;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

}
