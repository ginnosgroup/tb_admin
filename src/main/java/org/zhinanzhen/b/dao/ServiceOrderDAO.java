package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;

import java.util.List;

public interface ServiceOrderDAO {

    int addServiceOrder(ServiceOrderDO serviceOrderDo);

    int updateServiceOrder(ServiceOrderDO serviceOrderDo);

    int setCommission(@Param("id") Integer id, @Param("commissionAmount") Double commissionAmount, @Param("predictCommission") Double predictCommission, @Param("predictCommission1") Double predictCommission1);

    int updateReviewState(@Param("id") Integer id, @Param("reviewState") String reviewState);

    int updateService(@Param("id")Integer id,@Param("serviceId") Integer serviceId, @Param("serviceAssessId") Integer serviceAssessId);

    int countServiceOrder(@Param("type") String type, @Param("excludeTypeList") List<String> excludeTypeList,
                          @Param("excludeState") String excludeState, @Param("stateList") List<String> stateList,
                          @Param("auditingState") String auditingState, @Param("reviewStateList") List<String> reviewStateList,
                          @Param("urgentState") String urgentState, @Param("startMaraApprovalDate") String startMaraApprovalDate,
                          @Param("endMaraApprovalDate") String endMaraApprovalDate,
                          @Param("startOfficialApprovalDate") String startOfficialApprovalDate,
                          @Param("endOfficialApprovalDate") String endOfficialApprovalDate,
                          @Param("startReadcommittedDate") String startReadcommittedDate,
                          @Param("endReadcommittedDate") String endReadcommittedDate,
                          @Param("startFinishDate") String startFinishDate,
                          @Param("endFinishDate") String endFinishDate,
                          @Param("regionIdList") List<Integer> regionIdList, @Param("userId") Integer userId,
                          @Param("userName") String userName, @Param("applicantName") String applicantName,
                          @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
                          @Param("officialId") Integer officialId, @Param("officialTagId") Integer officialTagId,
                          @Param("parentId") Integer parentId, @Param("applicantParentId") Integer applicantParentId,
                          @Param("isNotApproved") Boolean isNotApproved, @Param("serviceId") Integer serviceId,
                          @Param("servicePackageId") Integer servicePackageId,
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
                                          @Param("startFinishDate") String startFinishDate,
                                          @Param("endFinishDate") String endFinishDate,
                                          @Param("regionIdList") List<Integer> regionIdList, @Param("userId") Integer userId,
                                          @Param("userName") String userName, @Param("applicantName") String applicantName,
                                          @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
                                          @Param("officialId") Integer officialId, @Param("officialTagId") Integer officialTagId,
                                          @Param("parentId") Integer parentId, @Param("applicantParentId") Integer applicantParentId,
                                          @Param("isNotApproved") Boolean isNotApproved, @Param("serviceId") Integer serviceId,
                                          @Param("servicePackageId") Integer servicePackageId,
                                          @Param("schoolId") Integer schoolId, @Param("isPay") Boolean isPay, @Param("isSettle") Boolean isSettle,
                                          @Param("offset") int offset, @Param("rows") int rows, @Param("orderBy") String orderBy);

    List<ServiceOrderDO> listByParentId(@Param("parentId") Integer parentId);
    
    List<ServiceOrderDO> listByApplicantParentId(@Param("applicantParentId") Integer applicantParentId);

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

    List<AdviserServiceCountDO> listServiceOrderToAnalysis(@Param("typeList") List<String> typeList, @Param("month") int month,
                                                           @Param("regionIdList") List<String> regionIdList);

    /**
     * 服务订单按照顾问地区,新学校库专业分组
     */
    List<EachRegionNumberDO> listOvstServiceOrderGroupByForRegion(@Param("startOfficialApprovalDate") String startOfficialApprovalDate,
                                                                  @Param("endOfficialApprovalDate") String endOfficialApprovalDate);

    //查看服务订单对应佣金信息
    List<VisaDO> getCommissionOrderList(@Param("id") int id
    );

    //查看离职文案需交接订单
    List<ServiceOrderDO> OfficialHandoverServiceOrder(@Param("officialId") Integer officialId
    );

    int updateOffice(@Param("officialId") Integer officialId,
                     @Param("id") Integer id);
    //获取多个申请人列表
    List<ApplicantListDO> ApplicantListByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

    // 根据父订单获取子订单
    List<ServiceOrderDTO> getDeriveOrder(@Param("id") int id);

    @Select("SELECT * FROM b_service_order WHERE applicant_parent_id = #{id}")
    List<ServiceOrderDTO> getZiOrder(int id);

    List<ServiceOrderDO> getTmpServiceOrder(@Param("beforeFormat")String beforeFormat, @Param("nowDate")String nowDate);


}