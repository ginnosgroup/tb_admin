package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderReviewDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServiceOrderService {

	int addServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException;

	int updateServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException;

	int countServiceOrder(String type, String state, int userId, int maraId, int adviserId, int officialId)
			throws ServiceException;

	List<ServiceOrderDTO> listServiceOrder(String type, String state, int userId, int maraId, int adviserId,
			int officialId, int pageNum, int pageSize) throws ServiceException;

	ServiceOrderDTO getServiceOrderById(int id) throws ServiceException;

	int deleteServiceOrderById(int id) throws ServiceException;
	
	int finish(int id) throws ServiceException;

	ServiceOrderDTO approval(int id, int adminUserId, String adviserState, String maraState,
			String officialState, String kjState) throws ServiceException;

	ServiceOrderDTO refuse(int id, int adminUserId, String adviserState, String maraState, String officialState,
			String kjState) throws ServiceException;
	
	List<ServiceOrderReviewDTO> reviews(int serviceOrderId) throws ServiceException;
}
