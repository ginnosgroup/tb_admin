package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.RefoundReportDTO;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface RefundService {

	int addRefund(RefundDTO refundDto) throws ServiceException;

	int countRefund(String type, String state, Integer visaId, Integer commissionOrderId, Integer regionId,
			Integer adviserId, String adviserName, String startDate, String endDate, String startReviewedDate,
			String endReviewedDate, String startCompletedDate, String endCompletedDate) throws ServiceException;

	List<RefundDTO> listRefund(String type, String state, Integer visaId, Integer commissionOrderId, Integer regionId,
			Integer adviserId, String adviserName, String startDate, String endDate, String startReviewedDate,
			String endReviewedDate, String startCompletedDate, String endCompletedDate, int pageNum, int pageSize)
			throws ServiceException;

	RefundDTO getRefundById(int id) throws ServiceException;

	int updateRefund(RefundDTO refundDto) throws ServiceException;

	int deleteRefundById(int id) throws ServiceException;

	List<RefoundReportDTO> listRefundReport(String startDate, String endDate, String dateType, String dateMethod,
			Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException;
	
	List<RefoundReportDTO> listRefundReport2(String startDate, String endDate, String dateType, String dateMethod,
			Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException;

	List<RefoundReportDTO> listRefundReportSubtractGst(String startDate, String endDate, String dateType, String dateMethod,
													   Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException;
}
