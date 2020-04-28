package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServicePackageDO;

public interface ServicePackageDAO {

	public int add(ServicePackageDO servicePackageDO);

	public List<ServicePackageDO> list(@Param("serviceId") Integer serviceId);

	public ServicePackageDO getById(int id);

	int delete(int id);

}
