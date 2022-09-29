package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.pojo.ReceiveTypeDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;

public interface ServiceOrderDAO {

	int addServiceOrder(ServiceOrderDO serviceOrderDo);

	int updateServiceOrder(ServiceOrderDO serviceOrderDo);

	int setCommission(@Param("id") Integer id, @Param("commissionAmount") Double commissionAmount,@Param("predictCommission") Double predictCommission,@Param("predictCommission1") Double predictCommission1);

	int updateReviewState(@Param("id") Integer id, @Param("reviewState") String reviewState);

	int countServiceOrder(@Param("type") String type, @Param("excludeTypeList") List<String> excludeTypeList,
			@Param("excludeState") String excludeState, @Param("stateList") List<String> stateList,
			@Param("auditingState") String auditingState, @Param("reviewStateList") List<String> reviewStateList,
			@Param("urgentState") String urgentState, @Param("startMaraApprovalDate") String startMaraApprovalDate,
			@Param("endMaraApprovalDate") String endMaraApprovalDate,
			@Param("startOfficialApprovalDate") String startOfficialApprovalDate,
			@Param("endOfficialApprovalDate") String endOfficialApprovalDate,
			@Param("startReadcommittedDate") String startReadcommittedDate,
			@Param("endReadcommittedDate") String endReadcommittedDate,
			@Param("regionIdList") List<Integer> regionIdList, @Param("userId") Integer userId,
			@Param("userName") String userName, @Param("applicantName") String applicantName,
			@Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId, @Param("officialTagId") Integer officialTagId,
			@Param("parentId") Integer parentId, @Param("applicantParentId") Integer applicantParentId,
			@Param("isNotApproved") Boolean isNotApproved, @Param("serviceId") Integer serviceId,
			@Param("schoolId") Integer schoolId, @Param("isPay") Boolean isPay, @Param("isSettle") Boolean isSettle);

	List<ServiceOrderDO> listServiceOrder(@Param("type") String type,
			@Param("excludeTypeList") List<String> excludeTypeList, @Param("excludeState") String excludeState,
			@Param("stateList") List<String> stateList, @Param("auditingState") String auditingState,
			@Param("reviewStateList") List<String> reviewStateList, @Param("urgentState") String urgentState,
			@Param("startMaraApprovalDate") String startMaraApprovalDate,
			@Param("endMaraApprovalDate") String endMaraApprovalDate,
			@Param("startOfficialApprovalDate") String startOfficialApprovalDate,
			@Param("endOfficialApprovalDate") String endOfficialApprovalDate,
			@Param("startReadcommittedDate") String startReadcommittedDate,
			@Param("endReadcommittedDate") String endReadcommittedDate,
			@Param("regionIdList") List<Integer> regionIdList, @Param("userId") Integer userId,
			@Param("userName") String userName, @Param("applicantName") String applicantName,
			@Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId, @Param("officialTagId") Integer officialTagId,
			@Param("parentId") Integer parentId, @Param("applicantParentId") Integer applicantParentId,
			@Param("isNotApproved") Boolean isNotApproved, @Param("serviceId") Integer serviceId,
			@Param("schoolId") Integer schoolId, @Param("isPay") Boolean isPay, @Param("isSettle") Boolean isSettle,
			@Param("offset") int offset, @Param("rows") int rows, @Param("orderBy") String orderBy);

	List<ServiceOrderDO> listByParentId(@Param("parentId") Integer parentId);

	ServiceOrderDO getServiceOrderById(int id);
	
	int countServiceOrderByApplicantId(int applicantId);

	int deleteServiceOrderById(int id);

	int finishServiceOrder(int id);

	int ReadcommittedServiceOrder(int id);

	List<EachRegionNumberDO> listServiceOrderGroupByForRegion(@Param("type") String type,
			@Param("startOfficialApprovalDate") String startOfficialApprovalDate,
			@Param("endOfficialApprovalDate") String endOfficialApprovalDate);

	List<ServiceOrderDO> listByVerifyCode(@Param("verifyCode") String verifyCode);

	List<EachSubjectCountDO> eachSubjectCount(@Param("startOfficialApprovalDate") String startOfficialApprovalDate,
			@Param("endOfficialApprovalDate") String endOfficialApprovalDate);

	List<ServiceOrderDO> NotReviewedServiceOrder(@Param("officialId") Integer officialId,
			@Param("thisMonth") boolean thisMonth);

	Integer caseCount(@Param("officialId") Integer officialId, @Param("days") String days,
			@Param("state") String state);

	List<AdviserServiceCountDO> listServiceOrderToAnalysis(@Param("typeList") List<String> typeList, @Param("month")int month ,
														   @Param("regionIdList") List<String> regionIdList);

	/**
	 *服务订单按照顾问地区,新学校库专业分组
	 */
	List<EachRegionNumberDO> listOvstServiceOrderGroupByForRegion(@Param("startOfficialApprovalDate") String startOfficialApprovalDate,
															   @Param("endOfficialApprovalDate") String endOfficialApprovalDate);
	//查看服务订单对应佣金信息
	List<CommissionOrderDO> getCommissionOrderList(@Param("id")int id);

	List<ServiceOrderDO> OfficialHandoverServiceOrder(@Param("officialId")Integer officialId,
													  @Param("isPackage") boolean isPackage);
}