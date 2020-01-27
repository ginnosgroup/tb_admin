package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReviewDO;

public interface ServiceOrderReviewDAO {

	public int addServiceOrderReview(ServiceOrderReviewDO serviceOrderReviewDo);

	public List<ServiceOrderReviewDO> listServiceOrderReview(@Param("serviceOrderId") Integer serviceOrderId,
			@Param("state") String state, @Param("type") String type, @Param("adminUserId") Integer adminUserId);

}
