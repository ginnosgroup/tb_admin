package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServiceOrderService {

	public int addServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException;

	public int updateServiceOrder(ServiceOrderDTO serviceOrderDto) throws ServiceException;

	public int countServiceOrder(String type, String state, int userId, int maraId, int adviserId, int officialId)
			throws ServiceException;

	public List<ServiceOrderDTO> listServiceOrder(String type, String state, int userId, int maraId, int adviserId,
			int officialId, int pageNum, int pageSize) throws ServiceException;

	public ServiceOrderDTO getServiceOrderById(int id) throws ServiceException;

	public int deleteServiceOrderById(int id) throws ServiceException;
	
	public ServiceOrderDTO Approval(int id) throws ServiceException;
	
	public ServiceOrderDTO Refuse(int id) throws ServiceException;

}
