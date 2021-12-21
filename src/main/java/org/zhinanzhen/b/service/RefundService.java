package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface RefundService {

	int addRefund(RefundDTO refundDto) throws ServiceException;

	List<RefundDTO> listRefund(String type, String state) throws ServiceException;

	RefundDTO getRefundById(int id) throws ServiceException;

	int updateRefund(RefundDTO refundDto) throws ServiceException;

	int deleteRefundById(int id) throws ServiceException;

}
