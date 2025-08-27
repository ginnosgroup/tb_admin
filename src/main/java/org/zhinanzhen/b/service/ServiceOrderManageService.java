package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.ServiceOrderAndManage;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

public interface ServiceOrderManageService {


    int addServiceOrderAndManage(ServiceOrderAndManage serviceOrderAndManage);

    int add(ServiceOrderDTO serviceOrderDto);

    int countServiceOrder(String type, List<String> excludeTypeList, String excludeState, List<String> stateList,
                          String auditingState, List<String> reviewStateList, String urgentState, String startMaraApprovalDate,
                          String endMaraApprovalDate, String startOfficialApprovalDate, String endOfficialApprovalDate,
                          String startReadcommittedDate, String endReadcommittedDate, String startFinishDate, String endFinishDate, List<Integer> adviserRegionIdList,List<Integer> officialRegionIdList, Integer userId,
                          String userName, String applicantName, Integer maraId, Integer adviserId, Integer officialId,
                          Integer officialTagId, int parentId, int applicantParentId, boolean isNotApproved, Integer serviceId, Integer servicePackageId,
                          Integer schoolId, Boolean isPay, Boolean isSettle, Boolean bindingList, Integer courseId, String tradingName, Integer schoolLocation) throws ServiceException;

    List<ServiceOrderDTO> listServiceOrder(String type, List<String> excludeTypeList, String excludeState,
                                           List<String> stateList, String auditingState, List<String> reviewStateList, String urgentState,
                                           String startMaraApprovalDate, String endMaraApprovalDate, String startOfficialApprovalDate,
                                           String endOfficialApprovalDate, String startReadcommittedDate, String endReadcommittedDate, String startFinishDate, String endFinishDate,
                                           List<Integer> adviserRegionIdList, List<Integer> officialRegionIdList, Integer userId, String userName, String applicantName, Integer maraId,
                                           Integer adviserId, Integer officialId, Integer officialTagId, int parentId, int applicantParentId,
                                           boolean isNotApproved, int pageNum, int pageSize, Sorter sorter, Integer serviceId, Integer servicePackageId, Integer schoolId,
                                           Boolean isPay, Boolean isSettle, Boolean bindingList, Integer courseId, String tradingName, Integer schoolLocation) throws ServiceException;

    ServiceOrderDTO getServiceOrderById(Integer id) throws ServiceException;

    int updateServiceOrderManage(ServiceOrderDTO serviceorderManageDto) throws ServiceException;

}
