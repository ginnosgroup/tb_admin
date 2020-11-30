package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServiceAssessDao;
import org.zhinanzhen.b.dao.pojo.ServiceAssessDO;
import org.zhinanzhen.b.service.ServiceAssessService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/11/25 17:03
 * Description:
 * Version: V1.0
 */
@Service
public class ServiceAssessServiceImpl implements ServiceAssessService {

    @Resource
    private ServiceAssessDao serviceAssessDao;

    @Override
    public List<ServiceAssessDO> list(Integer serviceId) {
        return serviceAssessDao.list(serviceId);
    }

    @Override
    public int add(String name, Integer serviceId) {
        return serviceAssessDao.add(name,serviceId);
    }

    @Override
    public int update(Integer id, String name) {
        return serviceAssessDao.update(id,name);
    }

    @Override
    public int delete(Integer id) {
        return serviceAssessDao.delete(id);
    }

    @Override
    public ServiceAssessDO seleteAssessById(String serviceAssessId) {
        return serviceAssessDao.seleteAssessById(serviceAssessId);
    }

    @Override
    public List<ServiceAssessDO> seleteAssessByServiceId(String serviceId) {
        return serviceAssessDao.seleteAssessByServiceId(serviceId);
    }
}
