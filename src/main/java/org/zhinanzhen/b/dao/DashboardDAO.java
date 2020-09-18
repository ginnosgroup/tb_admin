package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;

public interface DashboardDAO {

    public Double getThisMonthExpectAmount(@Param("adviserId") Integer adviserId);

}
