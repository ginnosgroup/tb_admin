package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;

public interface DashboardDAO {

    public Double getThisMonthVisaExpectAmount(@Param("adviserId") Integer adviserId);

    public Double getThisMonthbCommissionOrderExpectAmountSBBTM(@Param("adviserId") Integer adviserId);

}
