package org.zhinanzhen.tb.service;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.tb.service.pojo.OrderDTO;

public interface OrderService {

	public int countOrder(Integer id, String name, Integer regionId, OrderStateEnum state, String userName,
			String userPhone) throws ServiceException;

	public List<OrderDTO> listOrder(Integer id, String name, Integer regionId, OrderStateEnum state, String userName,
			String userPhone, int pageNum, int pageSize) throws ServiceException;

	public List<OrderDTO> listOrderBySubjectParentId(int subjectParentId) throws ServiceException;

	public OrderDTO getOrderById(int id) throws ServiceException;

	public boolean allocatingAdviser(int id, int adviserId) throws ServiceException;

	public List<OrderDTO> listOrder(int userId, String classify) throws ServiceException;

	boolean endingMoney(int orderId, double remainPayAmount, double remainPayBalance, Date remainPayDate)
			throws ServiceException;
}
