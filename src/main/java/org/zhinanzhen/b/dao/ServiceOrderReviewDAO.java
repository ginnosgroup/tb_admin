package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReviewDO;

public interface ServiceOrderReviewDAO {

	int addServiceOrderReview(ServiceOrderReviewDO serviceOrderReviewDo);

	List<ServiceOrderReviewDO> listServiceOrderReview(@Param("serviceOrderId") Integer serviceOrderId,
			@Param("adviserState") String adviserState, @Param("maraState") String maraState,
			@Param("officialState") String officialState, @Param("kjState") String kjState, @Param("type") String type);

}
