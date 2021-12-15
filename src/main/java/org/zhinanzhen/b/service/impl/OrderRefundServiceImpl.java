package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.OrderRefundDAO;
import org.zhinanzhen.b.dao.pojo.OrderRefundDO;
import org.zhinanzhen.b.service.OrderRefundService;
import org.zhinanzhen.b.service.pojo.OrderRefundDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service
public class OrderRefundServiceImpl extends BaseService implements OrderRefundService {

	@Resource
	OrderRefundDAO orderRefundDao;

	@Override
	public int addOrderRefund(OrderRefundDTO orderRefundDto) throws ServiceException {
		if (orderRefundDto == null) {
			ServiceException se = new ServiceException("orderRefundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			OrderRefundDO orderRefundDo = mapper.map(orderRefundDto, OrderRefundDO.class);
			if (orderRefundDao.addOrderRefund(orderRefundDo) > 0) {
				orderRefundDto.setId(orderRefundDo.getId());
				return orderRefundDo.getId();
			} else {
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<OrderRefundDTO> listOrderRefund(String type, Integer visaId, Integer commissionOrderId, String state)
			throws ServiceException {

		List<OrderRefundDTO> orderRefundDtoList = new ArrayList<>();
		List<OrderRefundDO> orderRefundDoList = null;
		try {
			orderRefundDoList = orderRefundDao.listOrderRefund(type, visaId, commissionOrderId, state);
			if (orderRefundDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (OrderRefundDO orderRefundDo : orderRefundDoList) {
			OrderRefundDTO orderRefundDto = mapper.map(orderRefundDo, OrderRefundDTO.class);
			orderRefundDtoList.add(orderRefundDto);
		}
		return orderRefundDtoList;
	}
	
	@Override
	public OrderRefundDTO getOrderRefundById(int id) throws ServiceException {
		OrderRefundDO orderRefundDo = null;
		try {
			orderRefundDo = orderRefundDao.getOrderRefundById(id);
			if (orderRefundDo == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return mapper.map(orderRefundDo, OrderRefundDTO.class);
	}

	@Override
	public int updateOrderRefund(OrderRefundDTO orderRefundDto) throws ServiceException {
		if (orderRefundDto == null) {
			ServiceException se = new ServiceException("orderRefundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			OrderRefundDO orderRefundDo = mapper.map(orderRefundDto, OrderRefundDO.class);
			return orderRefundDao.updateOrderRefund(orderRefundDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteOrderRefundById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return orderRefundDao.deleteOrderRefundById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
