package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.BrokerageDAO;
import org.zhinanzhen.b.dao.BrokerageSaDAO;
import org.zhinanzhen.b.dao.RefundDAO;
import org.zhinanzhen.b.dao.SchoolBrokerageSaDAO;
import org.zhinanzhen.b.service.DashboardService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

@Service("DashboardService")
public class DashboardServiceImpl extends BaseService implements DashboardService {

	@Resource
	private BrokerageDAO brokerageDao;

	@Resource
	private BrokerageSaDAO brokerageSaDao;

	@Resource
	private SchoolBrokerageSaDAO schoolBrokerageSaDao;

	@Resource
	private RefundDAO refundDao;

	@Override
	public int getThisMonthExpectAmount() throws ServiceException {
		return brokerageDao.sumBonusByThisMonth() + brokerageSaDao.sumBonusByThisMonth()
				+ schoolBrokerageSaDao.sumBonusByThisMonth() - refundDao.sumRefundByThisMonth();
	}

}
