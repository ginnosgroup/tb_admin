package org.zhinanzhen.b.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.CommissionOrderDAO;
import org.zhinanzhen.b.dao.ServiceOrderReviewDAO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReviewDO;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("CommissionOrderService")
public class CommissionOrderServiceImpl extends BaseService implements CommissionOrderService {

	@Resource
	private CommissionOrderDAO commissionOrderDao;

	@Resource
	private ServiceOrderReviewDAO serviceOrderReviewDao;

	@Override
	@Transactional
	public int addCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException {
		if (commissionOrderDto == null) {
			ServiceException se = new ServiceException("commissionOrderDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			CommissionOrderDO commissionOrderDo = mapper.map(commissionOrderDto, CommissionOrderDO.class);
			List<ServiceOrderReviewDO> serviceOrderReviews = serviceOrderReviewDao
					.listServiceOrderReview(commissionOrderDo.getServiceOrderId(), null, null, null, null, "OVST");
			if (serviceOrderReviews == null || serviceOrderReviews.size() == 0) {
				ServiceException se = new ServiceException("服务订单需要审核后才能创建佣金订单!");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
			if (commissionOrderDao.addCommissionOrder(commissionOrderDo) > 0) {
				commissionOrderDto.setId(commissionOrderDo.getId());
				ServiceOrderReviewDO serviceOrderReviewDo = serviceOrderReviews.get(0);
				serviceOrderReviewDo.setCommissionOrderId(commissionOrderDo.getId());
				serviceOrderReviewDao.addServiceOrderReview(serviceOrderReviewDo);
				return commissionOrderDo.getId();
			} else
				return 0;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException {
		if (commissionOrderDto == null) {
			ServiceException se = new ServiceException("commissionOrderDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			CommissionOrderDO commissionOrderDo = mapper.map(commissionOrderDto, CommissionOrderDO.class);
			return commissionOrderDao.updateCommissionOrder(commissionOrderDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public CommissionOrderDTO getCommissionOrderById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		CommissionOrderDTO commissionOrderDto = null;
		try {
			CommissionOrderDO commissionOrderDo = commissionOrderDao.getCommissionOrderById(id);
			if (commissionOrderDo == null)
				return null;
			commissionOrderDto = mapper.map(commissionOrderDo, CommissionOrderDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return commissionOrderDto;
	}

}
