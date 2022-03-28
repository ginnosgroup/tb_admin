package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.service.DashboardService;
import org.zhinanzhen.b.service.pojo.DashboardAmountSummaryDTO;
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
		Double bCommissionOrderExpectAmountSBBTM = dashboardDAO.getThisMonthbCommissionOrderExpectAmountSBBTM(adviserId,
				regionIdList);

		return (visaExpectAmountSBBTM == null ? 0.00 : visaExpectAmountSBBTM)
				+ (bCommissionOrderExpectAmountSBBTM == null ? 0.00 : bCommissionOrderExpectAmountSBBTM);
	}

	@Override
	public double getVisaUnassignedBonusAmount() throws ServiceException {
		return dashboardDAO.getVisaUnassignedBonusAmount();
	}

	@Override
	public double getCommissionOrderUnassignedBonusAmount() throws ServiceException {
		return dashboardDAO.getCommissionOrderUnassignedBonusAmount();
	}

	@Override
	public double getCommissionOrderDZYUnassignedBonusAmount() throws ServiceException {
		return dashboardDAO.getCommissionOrderDZYUnassignedBonusAmount();
	}

	@Override
	public double getCommissionOrderSettleUnassignedBonusAmount() throws ServiceException {
		return dashboardDAO.getCommissionOrderSettleUnassignedBonusAmount();
	}

	@Override
	public DashboardAmountSummaryDTO summaryVisaUnassignedBonusAmount() throws ServiceException {
		return mapper.map(dashboardDAO.summaryVisaUnassignedBonusAmount(), DashboardAmountSummaryDTO.class);
	}

	@Override
	public DashboardAmountSummaryDTO summaryCommissionOrderUnassignedBonusAmount() throws ServiceException {
		return mapper.map(dashboardDAO.summaryCommissionOrderUnassignedBonusAmount(), DashboardAmountSummaryDTO.class);
	}

	@Override
	public DashboardAmountSummaryDTO summaryCommissionOrderDZYUnassignedBonusAmount() throws ServiceException {
		return mapper.map(dashboardDAO.summaryCommissionOrderDZYUnassignedBonusAmount(),
				DashboardAmountSummaryDTO.class);
	}

	@Override
	public DashboardAmountSummaryDTO summaryCommissionOrderSettleUnassignedBonusAmount() throws ServiceException {
		return mapper.map(dashboardDAO.summaryCommissionOrderSettleUnassignedBonusAmount(),
				DashboardAmountSummaryDTO.class);
	}

	@Override
	public DashboardAmountSummaryDTO summaryCommissionOrderDZYUnassignedBonusAmountGroupBySchool()
			throws ServiceException {
		return mapper.map(dashboardDAO.summaryCommissionOrderDZYUnassignedBonusAmountGroupBySchool(),
				DashboardAmountSummaryDTO.class);
	}

	@Override
	public DashboardAmountSummaryDTO summaryCommissionOrderSettleUnassignedBonusAmountGroupBySchool()
			throws ServiceException {
		return mapper.map(dashboardDAO.summaryCommissionOrderSettleUnassignedBonusAmountGroupBySchool(),
				DashboardAmountSummaryDTO.class);
	}

}
