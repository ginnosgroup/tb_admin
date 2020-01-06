package org.zhinanzhen.b.dao;

import java.util.List;

import org.zhinanzhen.b.dao.pojo.ServiceDO;

public interface ServiceDAO {

	public int addService(ServiceDO serviceDo);
	
	public int updateService(ServiceDO serviceDo);

	public List<ServiceDO> listService();

	public ServiceDO getServiceById(int id);

	public int deleteServiceById(int id);

}
