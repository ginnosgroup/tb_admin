package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServiceOrderOriginallyDAO;
import org.zhinanzhen.b.service.ServiceOrderOriginallyService;
import org.zhinanzhen.tb.dao.pojo.ServiceOrderOriginallyDO;

import javax.annotation.Resource;
import java.util.List;

@Service("ServiceOrderOriginallyService")
public class ServiceOrderOriginallyServiceImpl implements ServiceOrderOriginallyService {
    @Resource
    private ServiceOrderOriginallyDAO serviceOrderOriginallyDAO;

    @Override
    public int addServiceOrderOriginallyDO(ServiceOrderOriginallyDO serviceOrderOriginallyDO) {
        return serviceOrderOriginallyDAO.addServiceOrderOriginallyDO(serviceOrderOriginallyDO);
    }

    @Override
    public List<ServiceOrderOriginallyDO> listServiceOrderOriginallyDO(Integer serviceOrderId, Integer webLogId, Integer userId) {
        return serviceOrderOriginallyDAO.listServiceOrderOriginallyDO(serviceOrderId, webLogId, userId);
    }
}
