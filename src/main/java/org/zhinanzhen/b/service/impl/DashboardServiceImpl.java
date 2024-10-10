package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.DashboardAmountSummaryDO;
import org.zhinanzhen.b.service.DashboardService;
import org.zhinanzhen.b.service.pojo.DashboardAmountSummaryDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.utils.ListUtil;

import java.math.BigDecimal;
import java.util.List;

@Service("DashboardService")
public class DashboardServiceImpl extends BaseService implements DashboardService {

	@Resource
	private CommissionOrderDAO commissionOrderDao;

	@Resource
	private DashboardDAO dashboardDAO;

	@Override
	public double getThisMonthExpectAmount(Integer adviserId, List<Integer> regionIdList, Double rate)
			throws ServiceException {
		Double visaExpectAmountSBBTM = dashboardDAO.getThisMonthVisaExpectAmount(adviserId, regionIdList);
		Double bCommissionOrderExpectAmountSBBTM = dashboardDAO.getThisMonthbCommissionOrderExpectAmountSBBTM(adviserId,
				regionIdList);
		Double thisMonthRefundAmount = dashboardDAO.getThisMonthRefundAmount(adviserId, regionIdList, rate);

		return (visaExpectAmountSBBTM == null ? 0.00 : visaExpectAmountSBBTM)
				+ (bCommissionOrderExpectAmountSBBTM == null ? 0.00 : bCommissionOrderExpectAmountSBBTM)
				- (thisMonthRefundAmount == null ? 0.00 : thisMonthRefundAmount);
	}

	@Override
	public double getThisMonthExpectAmountSubtractGst(Integer adviserId, List<Integer> regionIdList, Double rate)
			throws ServiceException {
		Double visaExpectAmountSBBTM = dashboardDAO.getThisMonthVisaExpectAmountSubtractGst(adviserId, regionIdList);
		Double bCommissionOrderExpectAmountSBBTM = dashboardDAO.getThisMonthbCommissionOrderExpectAmountSBBTMSubtractGst(adviserId,
				regionIdList);
		Double thisMonthRefundAmount = dashboardDAO.getThisMonthRefundAmount(adviserId, regionIdList, rate);
		double v = 0.00;
		if (thisMonthRefundAmount != null) {
			v = new BigDecimal(thisMonthRefundAmount - (thisMonthRefundAmount / 11)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		return (visaExpectAmountSBBTM == null ? 0.00 : visaExpectAmountSBBTM)
				+ (bCommissionOrderExpectAmountSBBTM == null ? 0.00 : bCommissionOrderExpectAmountSBBTM)
				- v;
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
	public List<DashboardAmountSummaryDTO> summaryVisaUnassignedBonusAmount() throws ServiceException {
		List<DashboardAmountSummaryDTO> dashboardAmountSummaryDtoList = ListUtil.newArrayList();
		List<DashboardAmountSummaryDO> dashboardAmountSummaryDoList = dashboardDAO.summaryVisaUnassignedBonusAmount();
		if (!ListUtil.isEmpty(dashboardAmountSummaryDoList))
			dashboardAmountSummaryDoList.forEach(dashboardAmountSummaryDo -> {
				dashboardAmountSummaryDtoList
						.add(mapper.map(dashboardAmountSummaryDo, DashboardAmountSummaryDTO.class));
			});
		for (int i = 0; i < dashboardAmountSummaryDtoList.size(); i++) {
			dashboardAmountSummaryDtoList.get(i).setId("Kj1a" + i);
		}
		return dashboardAmountSummaryDtoList;
	}

	@Override
	public List<DashboardAmountSummaryDTO> summaryCommissionOrderUnassignedBonusAmount() throws ServiceException {
		List<DashboardAmountSummaryDTO> dashboardAmountSummaryDtoList = ListUtil.newArrayList();
		List<DashboardAmountSummaryDO> dashboardAmountSummaryDoList = dashboardDAO
				.summaryCommissionOrderUnassignedBonusAmount();
		if (!ListUtil.isEmpty(dashboardAmountSummaryDoList))
			dashboardAmountSummaryDoList.forEach(dashboardAmountSummaryDo -> {
				dashboardAmountSummaryDtoList
						.add(mapper.map(dashboardAmountSummaryDo, DashboardAmountSummaryDTO.class));
			});
		for (int i = 0; i < dashboardAmountSummaryDtoList.size(); i++) {
			dashboardAmountSummaryDtoList.get(i).setId("Kj1b" + i);
		}
		return dashboardAmountSummaryDtoList;
	}

	@Override
	public List<DashboardAmountSummaryDTO> summaryCommissionOrderDZYUnassignedBonusAmount() throws ServiceException {
		List<DashboardAmountSummaryDTO> dashboardAmountSummaryDtoList = ListUtil.newArrayList();
		List<DashboardAmountSummaryDO> dashboardAmountSummaryDoList = dashboardDAO
				.summaryCommissionOrderDZYUnassignedBonusAmount();
		if (!ListUtil.isEmpty(dashboardAmountSummaryDoList))
			dashboardAmountSummaryDoList.forEach(dashboardAmountSummaryDo -> {
				dashboardAmountSummaryDtoList
						.add(mapper.map(dashboardAmountSummaryDo, DashboardAmountSummaryDTO.class));
			});
		return dashboardAmountSummaryDtoList;
	}

	@Override
	public List<DashboardAmountSummaryDTO> summaryCommissionOrderSettleUnassignedBonusAmount() throws ServiceException {
		List<DashboardAmountSummaryDTO> dashboardAmountSummaryDtoList = ListUtil.newArrayList();
		List<DashboardAmountSummaryDO> dashboardAmountSummaryDoList = dashboardDAO
				.summaryCommissionOrderSettleUnassignedBonusAmount();
		if (!ListUtil.isEmpty(dashboardAmountSummaryDoList))
			dashboardAmountSummaryDoList.forEach(dashboardAmountSummaryDo -> {
				dashboardAmountSummaryDtoList
						.add(mapper.map(dashboardAmountSummaryDo, DashboardAmountSummaryDTO.class));
			});
		return dashboardAmountSummaryDtoList;
	}

	@Override
	public List<DashboardAmountSummaryDTO> summaryCommissionOrderDZYUnassignedBonusAmountGroupBySchool()
			throws ServiceException {
		List<DashboardAmountSummaryDTO> dashboardAmountSummaryDtoList = ListUtil.newArrayList();
		List<DashboardAmountSummaryDO> dashboardAmountSummaryDoList = dashboardDAO
				.summaryCommissionOrderDZYUnassignedBonusAmountGroupBySchool();
		if (!ListUtil.isEmpty(dashboardAmountSummaryDoList))
			dashboardAmountSummaryDoList.forEach(dashboardAmountSummaryDo -> {
				dashboardAmountSummaryDtoList
						.add(mapper.map(dashboardAmountSummaryDo, DashboardAmountSummaryDTO.class));
			});
		return dashboardAmountSummaryDtoList;
	}

	@Override
	public List<DashboardAmountSummaryDTO> summaryCommissionOrderSettleUnassignedBonusAmountGroupBySchool()
			throws ServiceException {
		List<DashboardAmountSummaryDTO> dashboardAmountSummaryDtoList = ListUtil.newArrayList();
		List<DashboardAmountSummaryDO> dashboardAmountSummaryDoList = dashboardDAO
				.summaryCommissionOrderSettleUnassignedBonusAmountGroupBySchool();
		if (!ListUtil.isEmpty(dashboardAmountSummaryDoList))
			dashboardAmountSummaryDoList.forEach(dashboardAmountSummaryDo -> {
				dashboardAmountSummaryDtoList
						.add(mapper.map(dashboardAmountSummaryDo, DashboardAmountSummaryDTO.class));
			});
		return dashboardAmountSummaryDtoList;
	}

}
