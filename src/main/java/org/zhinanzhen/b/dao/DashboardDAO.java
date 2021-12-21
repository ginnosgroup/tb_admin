package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DashboardDAO {

    public Double getThisMonthVisaExpectAmount(@Param("adviserId") Integer adviserId,
                                               @Param("regionIdList")List<Integer> regionIdList);

    public Double getThisMonthbCommissionOrderExpectAmountSBBTM(@Param("adviserId") Integer adviserId,
                                                                @Param("regionIdList")List<Integer> regionIdList);

}
