package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface DashboardService {

	double getThisMonthExpectAmount(Integer adviserId) throws ServiceException;

	public List<CommissionOrderDTO> listThisMonthCommissionOrder() throws ServiceException;

}
