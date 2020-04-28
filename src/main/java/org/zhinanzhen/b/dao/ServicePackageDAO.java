package org.zhinanzhen.b.dao;

import java.util.List;

import org.zhinanzhen.b.dao.pojo.ServicePackageDO;

public interface ServicePackageDAO {

	public int add(ServicePackageDO servicePackageDO);

	public List<ServicePackageDO> listAll();

	public ServicePackageDO getById(int id);

}
