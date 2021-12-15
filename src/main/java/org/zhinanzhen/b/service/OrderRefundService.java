package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.OrderRefundDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface OrderRefundService {

	int addOrderRefund(OrderRefundDTO orderRefundDto) throws ServiceException;

	List<OrderRefundDTO> listOrderRefund(String type, Integer visaId, Integer commissionOrderId, String state)
			throws ServiceException;

	OrderRefundDTO getOrderRefundById(int id) throws ServiceException;

	int updateOrderRefund(OrderRefundDTO orderRefundDto) throws ServiceException;

	int deleteOrderRefundById(int id) throws ServiceException;

}
