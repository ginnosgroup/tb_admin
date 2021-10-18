package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServiceOrderService {

	int addServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException;

	int updateServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException;

	int updateServiceOrderRviewState(int id, String reviewState) throws ServiceException;

	int countServiceOrder(String type, List<String> excludeTypeList, String excludeState, List<String> stateList,
			String auditingState, List<String> reviewStateList, String urgentState, String startMaraApprovalDate,
			String endMaraApprovalDate, String startOfficialApprovalDate, String endOfficialApprovalDate,
			String startReadcommittedDate, String endReadcommittedDate, List<Integer> regionIdList, Integer userId,
			String userName, Integer maraId, Integer adviserId, Integer officialId, Integer officialTagId, int parentId,
			boolean isNotApproved, Integer serviceId, Integer schoolId, Boolean isPay, Boolean isSettle) throws ServiceException;

	List<ServiceOrderDTO> listServiceOrder(String type, List<String> excludeTypeList, String excludeState,
			List<String> stateList, String auditingState, List<String> reviewStateList, String urgentState,
			String startMaraApprovalDate, String endMaraApprovalDate, String startOfficialApprovalDate,
			String endOfficialApprovalDate, String startReadcommittedDate, String endReadcommittedDate,
			List<Integer> regionIdList, Integer userId, String userName, Integer maraId, Integer adviserId,
			Integer officialId, Integer officialTagId, int parentId, boolean isNotApproved, int pageNum, int pageSize,
			Sorter sorter, Integer serviceId, Integer schoolId, Boolean isPay, Boolean isSettle) throws ServiceException;

	ServiceOrderDTO getServiceOrderById(int id) throws ServiceException;

	int deleteServiceOrderById(int id) throws ServiceException;

	int finish(int id) throws ServiceException;

	int Readcommitted(int id) throws ServiceException;

	ServiceOrderDTO approval(int id, int adminUserId, String adviserState, String maraState, String officialState,
			String kjState) throws ServiceException;

	ServiceOrderDTO refuse(int id, int adminUserId, String adviserState, String maraState, String officialState,
			String kjState) throws ServiceException;

	void sendRemind(int id, String state);

	@Deprecated
	List<ServiceOrderReviewDTO> reviews(int serviceOrderId) throws ServiceException;

	int addComment(ServiceOrderCommentDTO serviceOrderCommentDto) throws ServiceException;

	List<ServiceOrderCommentDTO> listComment(int id) throws ServiceException;

	int deleteComment(int id) throws ServiceException;

	int addOfficialRemarks(ServiceOrderOfficialRemarksDTO serviceOrderOfficialRemarksDto) throws ServiceException;

	int updateOfficialRemarks(ServiceOrderOfficialRemarksDTO serviceOrderOfficialRemarksDto) throws ServiceException;

	List<ServiceOrderOfficialRemarksDTO> listOfficialRemarks(int id, int officialId) throws ServiceException;

	int deleteServiceOrderOfficialRemarksDTO(int id) throws ServiceException;

	List<EachRegionNumberDTO> listServiceOrderGroupByForRegion(String type, String startOfficialApprovalDate,
			String endOfficialApprovalDate);

	List<EachSubjectCountDTO> eachSubjectCount(String startOfficialApprovalDate, String endOfficialApprovalDate);

	List<ServiceOrderDTO> NotReviewedServiceOrder(Integer officialId, boolean thisMonth);

	Integer caseCount(Integer officialId, String days, String state);

	List<AdviserServiceCountDTO> listServiceOrderToAnalysis(List<String> typeList,int month,List<String> regionIdList);
}
