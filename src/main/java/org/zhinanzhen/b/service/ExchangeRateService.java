package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ExchangeRateDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ExchangeRateService {

	ExchangeRateDTO getExchangeRate() throws ServiceException;
	
	List<ExchangeRateDTO> listExchangeRate() throws ServiceException;

}
