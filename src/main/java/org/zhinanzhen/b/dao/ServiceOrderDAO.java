package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.EachRegionNumberDO;
import org.zhinanzhen.b.dao.pojo.EachSubjectCountDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;

public interface ServiceOrderDAO {

	int addServiceOrder(ServiceOrderDO serviceOrderDo);

	int updateServiceOrder(ServiceOrderDO serviceOrderDo);

	int updateReviewState(@Param("id") Integer id, @Param("reviewState") String reviewState);

	int countServiceOrder(@Param("type") String type, @Param("excludeState") String excludeState,
						  @Param("stateList") List<String> stateList, @Param("auditingState") String auditingState,
						  @Param("reviewStateList") List<String> reviewStateList,
						  @Param("startMaraApprovalDate") String startMaraApprovalDate,
						  @Param("endMaraApprovalDate") String endMaraApprovalDate,
						  @Param("startOfficialApprovalDate") String startOfficialApprovalDate,
						  @Param("endOfficialApprovalDate") String endOfficialApprovalDate,
						  @Param("startReadcommittedDate") String startReadcommittedDate,
						  @Param("endReadcommittedDate") String endReadcommittedDate,
						  @Param("regionIdList") List<Integer> regionIdList, @Param("userId") Integer userId,
						  @Param("userName") String userName,
						  @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
						  @Param("officialId") Integer officialId, @Param("officialTagId") Integer officialTagId,
						  @Param("parentId") Integer parentId, @Param("isNotApproved") Boolean isNotApproved,
						  @Param("serviceId") Integer serviceId, @Param("schoolId") Integer schoolId,
						  @Param("isPay")boolean isPay);

	List<ServiceOrderDO> listServiceOrder(@Param("type") String type, @Param("excludeState") String excludeState,
										  @Param("stateList") List<String> stateList, @Param("auditingState") String auditingState,
										  @Param("reviewStateList") List<String> reviewStateList,
										  @Param("startMaraApprovalDate") String startMaraApprovalDate,
										  @Param("endMaraApprovalDate") String endMaraApprovalDate,
										  @Param("startOfficialApprovalDate") String startOfficialApprovalDate,
										  @Param("endOfficialApprovalDate") String endOfficialApprovalDate,
										  @Param("startReadcommittedDate") String startReadcommittedDate,
										  @Param("endReadcommittedDate") String endReadcommittedDate,
										  @Param("regionIdList") List<Integer> regionIdList, @Param("userId") Integer userId,
										  @Param("userName") String userName,
										  @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
										  @Param("officialId") Integer officialId, @Param("officialTagId") Integer officialTagId,
										  @Param("parentId") Integer parentId, @Param("isNotApproved") Boolean isNotApproved,
										  @Param("serviceId") Integer serviceId, @Param("schoolId") Integer schoolId,
										  @Param("isPay")boolean isPay,
										  @Param("offset") int offset, @Param("rows") int rows, @Param("orderBy") String orderBy);

	List<ServiceOrderDO> listByParentId(@Param("parentId") Integer parentId);

	ServiceOrderDO getServiceOrderById(int id);

	int deleteServiceOrderById(int id);

	int finishServiceOrder(int id);

	int ReadcommittedServiceOrder(int id);

	List<EachRegionNumberDO> listServiceOrderGroupByForRegion(@Param("type") String type,@Param("startOfficialApprovalDate") String startOfficialApprovalDate,
															  @Param("endOfficialApprovalDate") String endOfficialApprovalDate);

	List<ServiceOrderDO> listByVerifyCode(@Param("verifyCode") String verifyCode);

	List<EachSubjectCountDO> eachSubjectCount(@Param("startOfficialApprovalDate") String startOfficialApprovalDate,
										@Param("endOfficialApprovalDate") String endOfficialApprovalDate);

	List<ServiceOrderDO> NotReviewedServiceOrder(@Param("officialId")Integer officialId, @Param("thisMonth") boolean thisMonth);

	Integer caseCount(@Param("officialId") Integer officialId, @Param("days") String days,
					  @Param("state")String state);
}
