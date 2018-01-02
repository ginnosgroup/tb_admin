package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.BrokerageDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface BrokerageService {

	public int addBrokerage(BrokerageDTO brokerageDto) throws ServiceException;

	public int updateBrokerage(BrokerageDTO brokerageDto) throws ServiceException;

	public int countBrokerage(String keyword, String stardDate, String endDate, Integer adviserId)
			throws ServiceException;

	public List<BrokerageDTO> listBrokerage(String keyword, String stardDate, String endDate, Integer adviserId,
			int pageNum, int pageSize) throws ServiceException;

	public BrokerageDTO getBrokerageById(int id) throws ServiceException;

}
