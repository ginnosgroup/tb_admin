package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ServiceOrderCommentDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderReviewDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServiceOrderService {

	int addServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException;

	int updateServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException;

	int updateServiceOrderRviewState(int id, String reviewState) throws ServiceException;

	int countServiceOrder(String type, String excludeState, List<String> stateList, String auditingState, List<String> reviewStateList,
			String startMaraApprovalDate, String endMaraApprovalDate, String startOfficialApprovalDate,
			String endOfficialApprovalDate, List<Integer> regionIdList, Integer userId, Integer maraId,
			Integer adviserId, Integer officialId, int parentId, boolean isNotApproved) throws ServiceException;

	List<ServiceOrderDTO> listServiceOrder(String type, String excludeState, List<String> stateList, String auditingState,
			List<String> reviewStateList, String startMaraApprovalDate, String endMaraApprovalDate,
			String startOfficialApprovalDate, String endOfficialApprovalDate, List<Integer> regionIdList,
			Integer userId, Integer maraId, Integer adviserId, Integer officialId, int parentId, boolean isNotApproved,
			int pageNum, int pageSize) throws ServiceException;

	ServiceOrderDTO getServiceOrderById(int id) throws ServiceException;

	int deleteServiceOrderById(int id) throws ServiceException;

	int finish(int id) throws ServiceException;

	ServiceOrderDTO approval(int id, int adminUserId, String adviserState, String maraState, String officialState,
			String kjState) throws ServiceException;

	ServiceOrderDTO refuse(int id, int adminUserId, String adviserState, String maraState, String officialState,
			String kjState) throws ServiceException;

	List<ServiceOrderReviewDTO> reviews(int serviceOrderId) throws ServiceException;

	int addComment(ServiceOrderCommentDTO serviceOrderCommentDto) throws ServiceException;

	List<ServiceOrderCommentDTO> listComment(int id) throws ServiceException;

	int deleteComment(int id) throws ServiceException;
}
