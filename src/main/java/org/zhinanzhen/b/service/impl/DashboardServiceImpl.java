package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.BrokerageDAO;
import org.zhinanzhen.b.dao.BrokerageSaDAO;
import org.zhinanzhen.b.dao.CommissionOrderDAO;
import org.zhinanzhen.b.dao.RefundDAO;
import org.zhinanzhen.b.dao.SchoolBrokerageSaDAO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.service.DashboardService;
import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("DashboardService")
public class DashboardServiceImpl extends BaseService implements DashboardService {

	@Resource
	private BrokerageDAO brokerageDao;

	@Resource
	private BrokerageSaDAO brokerageSaDao;

	@Resource
	private SchoolBrokerageSaDAO schoolBrokerageSaDao;

	@Resource
	private RefundDAO refundDao;

	@Resource
	private CommissionOrderDAO commissionOrderDao;

	@Override
	public double getThisMonthExpectAmount(Integer adviserId) throws ServiceException {
		Double brokerageSBBTM = brokerageDao.sumBonusByThisMonth(adviserId);
		Double brokerageSaSBBTM = brokerageSaDao.sumBonusByThisMonth(adviserId);
		Double schoolBrokerageSaSBBTM = schoolBrokerageSaDao.sumBonusByThisMonth(adviserId);
		Double refundSBBTM = refundDao.sumRefundByThisMonth(adviserId);
		return (brokerageSBBTM == null ? 0.00 : brokerageSBBTM) + (brokerageSaSBBTM == null ? 0.00 : brokerageSaSBBTM)
				+ (schoolBrokerageSaSBBTM == null ? 0.00 : schoolBrokerageSaSBBTM)
				- (refundSBBTM == null ? 0.00 : refundSBBTM);
	}

	@Override
	public List<CommissionOrderDTO> listThisMonthCommissionOrder() throws ServiceException {
		List<CommissionOrderDTO> commissionOrderDtoList = new ArrayList<>();
		List<CommissionOrderDO> commissionOrderDoList = new ArrayList<>();
		try {
			commissionOrderDoList = commissionOrderDao.listThisMonthCommissionOrderAtDashboard();
			if (commissionOrderDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		commissionOrderDoList.forEach(commissionOrderDo -> commissionOrderDtoList
				.add(mapper.map(commissionOrderDo, CommissionOrderDTO.class)));
		return commissionOrderDtoList;
	}

}
