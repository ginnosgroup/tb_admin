package org.zhinanzhen.b.service;

import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

public interface DashboardService {

	double getThisMonthExpectAmount(Integer adviserId, List<Integer> regionIdList) throws ServiceException;

}
