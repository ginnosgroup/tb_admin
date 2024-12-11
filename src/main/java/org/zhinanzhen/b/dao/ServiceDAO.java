package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.zhinanzhen.b.dao.pojo.ServiceDO;

public interface ServiceDAO {

	public int addService(ServiceDO serviceDo);

	public int updateService(@Param("id") int id, @Param("name") String name, @Param("code") String code,
			@Param("role") String role);

	public int countService(@Param("name") String name, @Param("isZx") boolean isZx);

	public List<ServiceDO> listService(@Param("name") String name, @Param("isZx") boolean isZx,
			@Param("offset") int offset, @Param("rows") int rows);
	
	public int countAllService(@Param("name") String name);

	public List<ServiceDO> listAllService(@Param("name") String name, @Param("offset") int offset,
			@Param("rows") int rows);

	public ServiceDO getServiceById(int id);

	public int deleteServiceById(@Param("id") int id, @Param("isZx") boolean isZx);

	@Select("select code from b_service where `name` = '签证' and is_long_time = true and is_delete = false")
    List<String> listLongTimeVisa();

	@Select("select code from b_service where is_extra = true and is_delete = false")
	List<String> listExtraAmount();
}