package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface CommissionOrderService {

	int addCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException;

	int updateCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException;

	public int countCommissionOrder(Integer maraId, Integer adviserId, Integer officialId, String name, String phone,
			String wechatUsername, Integer schoolId, Boolean isSettle, String state) throws ServiceException;

	public List<CommissionOrderListDTO> listCommissionOrder(Integer maraId, Integer adviserId, Integer officialId,
			String name, String phone, String wechatUsername, Integer schoolId, Boolean isSettle, String state,
			int pageNum, int pageSize) throws ServiceException;

	CommissionOrderListDTO getCommissionOrderById(int id) throws ServiceException;

}
