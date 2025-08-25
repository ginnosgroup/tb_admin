package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServiceOrderManageDAO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderAndManage;
import org.zhinanzhen.b.service.ServiceOrderManageService;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;

@Service("ServiceOrderManageService")
public class ServiceOrderManageServiceImpl extends BaseService implements ServiceOrderManageService {

    @Resource
    private ServiceOrderManageDAO serviceOrderManageDAO;

    @Override
    public int addServiceOrderAndManage(ServiceOrderAndManage serviceOrderAndManage) {
        return 0;
    }
}
