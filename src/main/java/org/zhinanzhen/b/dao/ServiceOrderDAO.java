package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;

public interface ServiceOrderDAO {

	int addServiceOrder(ServiceOrderDO serviceOrderDo);

	int updateServiceOrder(ServiceOrderDO serviceOrderDo);

	int updateReviewState(@Param("id") Integer id, @Param("reviewState") String reviewState);

	int countServiceOrder(@Param("type") String type, @Param("excludeState") String excludeState,
			@Param("stateList") List<String> stateList, @Param("reviewStateList") List<String> reviewStateList,
			@Param("userId") Integer userId, @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId, @Param("parentId") Integer parentId);

	List<ServiceOrderDO> listServiceOrder(@Param("type") String type, @Param("excludeState") String excludeState,
			@Param("stateList") List<String> stateList, @Param("reviewStateList") List<String> reviewStateList,
			@Param("userId") Integer userId, @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId, @Param("parentId") Integer parentId, @Param("offset") int offset,
			@Param("rows") int rows);

	List<Integer> listServicePackageIdByParentId(@Param("parentId") Integer parentId);

	ServiceOrderDO getServiceOrderById(int id);

	int deleteServiceOrderById(int id);

	int finishServiceOrder(int id);

}
