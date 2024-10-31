package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.InsuranceCompanyDAO;
import org.zhinanzhen.b.dao.pojo.InsuranceCompanyDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderInsuranceDO;
import org.zhinanzhen.b.service.InsuranceCompanyService;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service("InsuranceCompanyService")
public class InsuranceCompanyServiceImpl implements InsuranceCompanyService {

    @Resource
    private InsuranceCompanyDAO insuranceCompanyDAO;

    @Override
    public List<InsuranceCompanyDO> list(Integer id, Boolean isSUPERAD, Integer offset, Integer rows) {
        return insuranceCompanyDAO.list(id, isSUPERAD, offset, rows);
    }

    @Override
    public Integer count(Integer id, boolean isSUPERAD) {
        return insuranceCompanyDAO.count(id, isSUPERAD);
    }

    @Override
    public Integer add(InsuranceCompanyDO insuranceCompanyDO) {
        return insuranceCompanyDAO.add(insuranceCompanyDO);
    }

    @Override
    public Integer update(InsuranceCompanyDO insuranceCompanyDO) {
        return insuranceCompanyDAO.update(insuranceCompanyDO);
    }

    @Override
    public ServiceOrderInsuranceDO listServiceOrderInsuranceDOByServiceOrderId(Integer serviceOrderId) {
        return insuranceCompanyDAO.listServiceOrderInsuranceDOByServiceOrderId(serviceOrderId);
    }
}
