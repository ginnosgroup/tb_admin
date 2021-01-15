package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServiceOrderReadcommittedDateDAO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReadcommittedDateDO;
import org.zhinanzhen.b.service.ServiceOrderReadcommittedDateService;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/01/07 14:07
 * Description:
 * Version: V1.0
 */
@Service
public class ServiceOrderReadcommittedDateServiceImpl implements ServiceOrderReadcommittedDateService {

    @Resource
    ServiceOrderReadcommittedDateDAO serviceOrderReadcommittedDateDAO;

    @Override
    public int add(ServiceOrderReadcommittedDateDO serviceOrderReadcommittedDateDO) {
        return serviceOrderReadcommittedDateDAO.add(serviceOrderReadcommittedDateDO);
    }
}
