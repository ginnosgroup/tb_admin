package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.InsuranceCompanyDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderInsuranceDO;

import java.util.List;

public interface InsuranceCompanyDAO {

    List<InsuranceCompanyDO> list(@Param("id") Integer id,@Param("isSUPERAD") Boolean isSUPERAD,@Param("offset") Integer offset,@Param("rows") Integer rows);

    Integer count(@Param("id") Integer id, @Param("isSUPERAD") boolean isSUPERAD);

    Integer add(InsuranceCompanyDO insuranceCompanyDO);

    Integer update(InsuranceCompanyDO insuranceCompanyDO);

    Integer addSserviceOrderInsurance(@Param("serviceOrderId") Integer serviceOrderId, @Param("insuranceCompanyId") Integer insuranceCompanyId);

    ServiceOrderInsuranceDO listServiceOrderInsuranceDOByServiceOrderId(Integer serviceOrderId);
}
