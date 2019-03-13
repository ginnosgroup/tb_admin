package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface RefundService {

	public int addRefund(RefundDTO refundDto) throws ServiceException;

	public int updateRefund(RefundDTO refundDto) throws ServiceException;

	public int countRefund(String keyword, String startHandlingDate, String endHandlingDate, String startDate,
			String endDate, Integer adviserId, Integer officialId, Integer userId) throws ServiceException;

	public List<RefundDTO> listRefund(String keyword, String startHandlingDate, String endHandlingDate,
			String startDate, String endDate, Integer adviserId, Integer officialId, Integer userId, int pageNum,
			int pageSize) throws ServiceException;

	public RefundDTO getRefundById(int id) throws ServiceException;

	public int deleteRefundById(int id) throws ServiceException;

}