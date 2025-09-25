package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceOrderAndManage;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;

import java.util.List;

public interface ServiceOrderManageDAO {


    int addServiceOrderAndManage(ServiceOrderAndManage serviceOrderAndManage);

    int add(ServiceOrderDTO serviceOrderDto);

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
                          @Param("adviserRegionIdList") List<Integer> adviserRegionIdList, @Param("officialRegionIdList") List<Integer> officialRegionIdList,
                          @Param("userId") Integer userId,
                          @Param("userName") String userName, @Param("applicantName") String applicantName,
                          @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
                          @Param("officialId") Integer officialId, @Param("officialTagId") Integer officialTagId,
                          @Param("parentId") Integer parentId, @Param("applicantParentId") Integer applicantParentId,
                          @Param("isNotApproved") Boolean isNotApproved, @Param("serviceId") Integer serviceId,
                          @Param("servicePackageId") Integer servicePackageId,
                          @Param("schoolId") Integer schoolId, @Param("isPay") Boolean isPay, @Param("isSettle") Boolean isSettle,
                          @Param("courseId") Integer courseId,
                          @Param("tradingName") String tradingName, @Param("schoolLocation") Integer schoolLocation);

    List<ServiceOrderDO> listServiceOrder(@Param("startGmtCreate") String startGmtCreate,
                                          @Param("endGmtCreate") String endGmtCreate,
                                          @Param("type") String type,
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
                                          @Param("adviserRegionIdList") List<Integer> adviserRegionIdList, @Param("officialRegionIdList") List<Integer> officialRegionIdList,
                                          @Param("userId") Integer userId,
                                          @Param("userName") String userName, @Param("applicantName") String applicantName,
                                          @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
                                          @Param("officialId") Integer officialId, @Param("officialTagId") Integer officialTagId,
                                          @Param("parentId") Integer parentId, @Param("applicantParentId") Integer applicantParentId,
                                          @Param("isNotApproved") Boolean isNotApproved, @Param("serviceId") Integer serviceId,
                                          @Param("servicePackageId") Integer servicePackageId,
                                          @Param("schoolId") Integer schoolId, @Param("isPay") Boolean isPay, @Param("isSettle") Boolean isSettle,
                                          @Param("bindingOrder") Integer bindingOrder,
                                          @Param("offset") int offset, @Param("rows") int rows, @Param("orderBy") String orderBy, @Param("courseId") Integer courseId,
                                          @Param("tradingName") String tradingName, @Param("schoolLocation") Integer schoolLocation);

    List<ServiceOrderDO> listSub(@Param("id")int id);

    ServiceOrderDO getServiceOrderById(Integer id);

    ServiceOrderAndManage getServiceOrderAndManageById(Integer id);

}
