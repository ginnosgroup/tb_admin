package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.BrokerageSaDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface BrokerageSaService {

	public int addBrokerageSa(BrokerageSaDTO brokerageSaDto) throws ServiceException;

	public int updateBrokerageSa(BrokerageSaDTO brokerageSaDto) throws ServiceException;

	public int countBrokerageSa(String keyword, String startHandlingDate, String endHandlingDate, String startDate,
			String endDate, Integer adviserId, Integer schoolId, Integer userId) throws ServiceException;

	public List<BrokerageSaDTO> listBrokerageSa(String keyword, String startHandlingDate, String endHandlingDate,
			String startDate, String endDate, Integer adviserId, Integer schoolId, Integer userId, int pageNum,
			int pageSize) throws ServiceException;

	public BrokerageSaDTO getBrokerageSaById(int id) throws ServiceException;

	public int deleteBrokerageSaById(int id) throws ServiceException;

}
