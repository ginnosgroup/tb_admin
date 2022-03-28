package org.zhinanzhen.b.service;

import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

public interface DashboardService {

	double getThisMonthExpectAmount(Integer adviserId, List<Integer> regionIdList) throws ServiceException;

	double getVisaUnassignedBonusAmount() throws ServiceException;

	double getCommissionOrderUnassignedBonusAmount() throws ServiceException;

	double getCommissionOrderDZYUnassignedBonusAmount() throws ServiceException;

	double getCommissionOrderSettleUnassignedBonusAmount() throws ServiceException;

}
