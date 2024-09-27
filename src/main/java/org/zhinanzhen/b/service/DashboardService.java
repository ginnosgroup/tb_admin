package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.DashboardAmountSummaryDTO;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

public interface DashboardService {

	double getThisMonthExpectAmount(Integer adviserId, List<Integer> regionIdList, Double rate) throws ServiceException;

	double getThisMonthExpectAmountSubtractGst(Integer adviserId, List<Integer> regionIdList, Double rate) throws ServiceException;

	double getVisaUnassignedBonusAmount() throws ServiceException;

	double getCommissionOrderUnassignedBonusAmount() throws ServiceException;

	double getCommissionOrderDZYUnassignedBonusAmount() throws ServiceException;

	double getCommissionOrderSettleUnassignedBonusAmount() throws ServiceException;

	List<DashboardAmountSummaryDTO> summaryVisaUnassignedBonusAmount() throws ServiceException;

	List<DashboardAmountSummaryDTO> summaryCommissionOrderUnassignedBonusAmount() throws ServiceException;

	List<DashboardAmountSummaryDTO> summaryCommissionOrderDZYUnassignedBonusAmount() throws ServiceException;

	List<DashboardAmountSummaryDTO> summaryCommissionOrderSettleUnassignedBonusAmount() throws ServiceException;

	List<DashboardAmountSummaryDTO> summaryCommissionOrderDZYUnassignedBonusAmountGroupBySchool()
			throws ServiceException;

	List<DashboardAmountSummaryDTO> summaryCommissionOrderSettleUnassignedBonusAmountGroupBySchool()
			throws ServiceException;

}
