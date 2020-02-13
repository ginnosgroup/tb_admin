package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface CommissionOrderService {

	int addCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException;
	
	int updateCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException;
	
	CommissionOrderDTO getCommissionOrderById(int id) throws ServiceException;

}
