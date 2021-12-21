package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.service.DashboardService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import java.util.List;

@Service("DashboardService")
public class DashboardServiceImpl extends BaseService implements DashboardService {

	@Resource
	private CommissionOrderDAO commissionOrderDao;

	@Resource
	private DashboardDAO dashboardDAO;

	@Override
	public double getThisMonthExpectAmount(Integer adviserId, List<Integer> regionIdList) throws ServiceException {
		Double visaExpectAmountSBBTM = dashboardDAO.getThisMonthVisaExpectAmount(adviserId, regionIdList);
		Double bCommissionOrderExpectAmountSBBTM = dashboardDAO.getThisMonthbCommissionOrderExpectAmountSBBTM(adviserId, regionIdList);


		return (visaExpectAmountSBBTM == null ? 0.00 : visaExpectAmountSBBTM) +
				(bCommissionOrderExpectAmountSBBTM == null ? 0.00 : bCommissionOrderExpectAmountSBBTM);
	}

}
