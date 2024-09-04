package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.InsuranceCompanyDO;

import java.util.List;

public interface InsuranceCompanyService {

    public List<InsuranceCompanyDO> list(Integer id, Boolean isSUPERAD, Integer offset, Integer rows);

    Integer count(Integer id, boolean isSUPERAD);

    Integer add(InsuranceCompanyDO insuranceCompanyDO);

    Integer update(InsuranceCompanyDO insuranceCompanyDO);

}
