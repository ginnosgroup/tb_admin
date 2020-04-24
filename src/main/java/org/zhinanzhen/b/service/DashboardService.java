package org.zhinanzhen.b.service;

import org.zhinanzhen.tb.service.ServiceException;

public interface DashboardService {

	double getThisMonthExpectAmount(Integer adviserId) throws ServiceException;

}
