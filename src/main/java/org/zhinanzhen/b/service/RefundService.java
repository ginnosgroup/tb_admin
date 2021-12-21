package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ManualRefundDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ManualRefundService {

	int addManualRefund(ManualRefundDTO manualRefundDto) throws ServiceException;

	List<ManualRefundDTO> listManualRefund(String type, String state) throws ServiceException;

	ManualRefundDTO getManualRefundById(int id) throws ServiceException;

	int updateManualRefund(ManualRefundDTO manualRefundDto) throws ServiceException;

	int deleteManualRefundById(int id) throws ServiceException;

}
