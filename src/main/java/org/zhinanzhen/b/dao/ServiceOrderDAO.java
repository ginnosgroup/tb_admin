package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;

public interface ServiceOrderDAO {

	public int addServiceOrder(ServiceOrderDO serviceOrderDo);

	public int updateServiceOrder(ServiceOrderDO serviceOrderDo);

	public int countServiceOrder(@Param("type") String type, @Param("state") String state,
			@Param("userId") Integer userId, @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId);

	public List<ServiceOrderDO> listServiceOrder(@Param("type") String type, @Param("state") String state,
			@Param("userId") Integer userId, @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId, @Param("offset") int offset, @Param("rows") int rows);

	public ServiceOrderDO getServiceOrderById(int id);

	public int deleteServiceOrderById(int id);

}
