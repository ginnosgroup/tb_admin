package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.DashboardAmountSummaryDO;

import java.util.List;

public interface DashboardDAO {

	public Double getThisMonthVisaExpectAmount(@Param("adviserId") Integer adviserId,
			@Param("regionIdList") List<Integer> regionIdList);

	public Double getThisMonthbCommissionOrderExpectAmountSBBTM(@Param("adviserId") Integer adviserId,
			@Param("regionIdList") List<Integer> regionIdList);
	
	public Double getThisMonthRefundAmount(@Param("adviserId") Integer adviserId,
			@Param("regionIdList") List<Integer> regionIdList);

	Double getVisaUnassignedBonusAmount();

	Double getCommissionOrderUnassignedBonusAmount();

	Double getCommissionOrderDZYUnassignedBonusAmount();

	Double getCommissionOrderSettleUnassignedBonusAmount();

	List<DashboardAmountSummaryDO> summaryVisaUnassignedBonusAmount();

	List<DashboardAmountSummaryDO> summaryCommissionOrderUnassignedBonusAmount();

	List<DashboardAmountSummaryDO> summaryCommissionOrderDZYUnassignedBonusAmount();

	List<DashboardAmountSummaryDO> summaryCommissionOrderSettleUnassignedBonusAmount();

	List<DashboardAmountSummaryDO> summaryCommissionOrderDZYUnassignedBonusAmountGroupBySchool();

	List<DashboardAmountSummaryDO> summaryCommissionOrderSettleUnassignedBonusAmountGroupBySchool();

}
