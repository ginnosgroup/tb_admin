package org.zhinanzhen.b.service;

import java.util.Date;
import java.util.List;


import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.service.ServiceException;

public interface CommissionOrderService {

	int addCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException;

	Integer addCommissionInfoById(int id, int installment_num) throws ServiceException;

	int updateCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException;

	int setinstallmentById(int id, int installment) throws ServiceException;

	int setinstallmentDueDateById(int id, int installment_num, Date installmentDueDate) throws ServiceException;

	public int countCommissionOrder(Integer id, List<Integer> regionIdList, Integer maraId, Integer adviserId,
									Integer officialId, Integer userId, String name, String applicantName, String phone, String wechatUsername,
									Integer schoolId, Boolean isSettle, List<String> stateList, List<String> commissionStateList,
									String startKjApprovalDate, String endKjApprovalDate, String startDate, String endDate,
									String startInvoiceCreate, String endInvoiceCreate, Boolean isYzyAndYjy, String applyState)
			throws ServiceException;

	public List<CommissionOrderListDTO> listCommissionOrder(Integer id, List<Integer> regionIdList, Integer maraId,
															Integer adviserId, Integer officialId, Integer userId, String name, String applicantName, String phone,
															String wechatUsername, Integer schoolId, Boolean isSettle, List<String> stateList,
															List<String> commissionStateList, String startKjApprovalDate, String endKjApprovalDate, String startDate,
															String endDate, String startInvoiceCreate, String endInvoiceCreate, Boolean isYzyAndYjy, String applyState,
															int pageNum, int pageSize, Sorter sorter) throws ServiceException, InterruptedException;

	List<CommissionOrderListDTO> listThisMonthCommissionOrder(Integer adviserId, Integer officialId)
			throws ServiceException;
	List<CommissionOrderListDTO> listHalfAYearCommissionOrder(Integer adviserId, Integer officialId) throws ServiceException;;

	List<CommissionOrderReportDTO> listCommissionOrderReport(String startDate, String endDate, String dateType,
															 String dateMethod, Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException;

	CommissionOrderListDTO getCommissionOrderById(int id) throws ServiceException;

	List<CommissionInfoDTO> getCommissionInfoById(int id, int adviserId) throws ServiceException;

	CommissionOrderListDTO getFirstCommissionOrderByServiceOrderId(int serviceOrderId) throws ServiceException;

	List<CommissionOrderListDTO> listCommissionOrderByInvoiceNumber(String invoiceNumber) throws ServiceException;

	int addComment(CommissionOrderCommentDTO commissionOrderCommentDto) throws ServiceException;

	List<CommissionOrderCommentDTO> listComment(int id) throws ServiceException;

	int deleteComment(int id) throws ServiceException;

	int deleteCommissionOrder(int id) throws ServiceException;

	int deleteCommissionOrderInfoById(int serviceOrderId, int installmentNum) throws ServiceException;

	void sendRefuseEmail(int id);

	int updateCommissionOrderByServiceOrderId(CommissionOrderDTO commissionOrderDTO) throws ServiceException;

	int addCommissionOrderTemp(CommissionOrderTempDTO tempDTO)throws ServiceException;

	CommissionOrderTempDTO getCommissionOrderTempByServiceOrderId(int id) throws ServiceException;

	int updateCommissionOrderTemp(CommissionOrderTempDTO tempDTO) throws ServiceException;

	CommissionOrderTempDTO getCommissionOrderTempById(int id) throws ServiceException;
	
	int confirmByInvoiceNo(String invoiceNo) throws ServiceException;


	List<CommissionOrderReportDTO> listCommissionOrderReportSubtractGst(String startDate, String endDate, String dateType,
																		String dateMethod, Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException;

	CommissionOrderDTO getCommissionOrderByServiceOrderId(Integer serviceOrderId);
}
