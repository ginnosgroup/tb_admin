package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.CommissionOrderCommentDTO;
import org.zhinanzhen.b.service.pojo.CommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface CommissionOrderService {

	int addCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException;

	int updateCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException;

	public int countCommissionOrder(Integer maraId, Integer adviserId, Integer officialId, String name, String phone,
			String wechatUsername, Integer schoolId, Boolean isSettle, List<String> stateList,
			List<String> commissionStateList, Boolean isYzyAndYjy) throws ServiceException;

	public List<CommissionOrderListDTO> listCommissionOrder(Integer maraId, Integer adviserId, Integer officialId,
			String name, String phone, String wechatUsername, Integer schoolId, Boolean isSettle,
			List<String> stateList, List<String> commissionStateList, Boolean isYzyAndYjy, int pageNum, int pageSize)
			throws ServiceException;

	List<CommissionOrderListDTO> listThisMonthCommissionOrder(Integer adviserId, Integer officialId)
			throws ServiceException;

	CommissionOrderListDTO getCommissionOrderById(int id) throws ServiceException;

	CommissionOrderListDTO getFirstCommissionOrderByServiceOrderId(int serviceOrderId) throws ServiceException;

	List<CommissionOrderListDTO> listCommissionOrderByInvoiceNumber(String invoiceNumber) throws ServiceException;

	int addComment(CommissionOrderCommentDTO commissionOrderCommentDto) throws ServiceException;

	List<CommissionOrderCommentDTO> listComment(int id) throws ServiceException;

	int deleteComment(int id) throws ServiceException;

}
