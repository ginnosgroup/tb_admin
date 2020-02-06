package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface CommissionOrderService {

	public int addCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException;

}
