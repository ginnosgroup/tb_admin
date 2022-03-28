package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.DashboardAmountSummaryDO;

import java.util.List;

public interface DashboardDAO {

    public Double getThisMonthVisaExpectAmount(@Param("adviserId") Integer adviserId,
                                               @Param("regionIdList")List<Integer> regionIdList);

    public Double getThisMonthbCommissionOrderExpectAmountSBBTM(@Param("adviserId") Integer adviserId,
                                                                @Param("regionIdList")List<Integer> regionIdList);
    
    Double getVisaUnassignedBonusAmount();
    
    Double getCommissionOrderUnassignedBonusAmount();
    
    Double getCommissionOrderDZYUnassignedBonusAmount();
    
    Double getCommissionOrderSettleUnassignedBonusAmount();
    
    DashboardAmountSummaryDO summaryVisaUnassignedBonusAmount();
    
    DashboardAmountSummaryDO summaryCommissionOrderUnassignedBonusAmount();
    
    DashboardAmountSummaryDO summaryCommissionOrderDZYUnassignedBonusAmount();
    
    DashboardAmountSummaryDO summaryCommissionOrderSettleUnassignedBonusAmount();
    
    DashboardAmountSummaryDO summaryCommissionOrderDZYUnassignedBonusAmountGroupBySchool();
    
    DashboardAmountSummaryDO summaryCommissionOrderSettleUnassignedBonusAmountGroupBySchool();

}
