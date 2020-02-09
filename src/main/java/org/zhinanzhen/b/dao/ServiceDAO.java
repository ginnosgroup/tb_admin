package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceDO;

public interface ServiceDAO {

	public int addService(ServiceDO serviceDo);

	public int updateService(@Param("id") int id, @Param("type") String type, @Param("code") String code);

	public List<ServiceDO> listService(@Param("type") String type);

	public ServiceDO getServiceById(int id);

	public int deleteServiceById(int id);

}