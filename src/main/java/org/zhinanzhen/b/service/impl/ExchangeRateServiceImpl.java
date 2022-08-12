package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.EverydayExchangeRateDAO;
import org.zhinanzhen.b.dao.pojo.EverydayExchangeRateDO;
import org.zhinanzhen.b.service.ExchangeRateService;
import org.zhinanzhen.b.service.pojo.ExchangeRateDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

@Service("ExchangeRateService")
public class ExchangeRateServiceImpl extends BaseService implements ExchangeRateService {

	@Resource
	private EverydayExchangeRateDAO everydayExchangeRateDao;

	@Override
	public ExchangeRateDTO getExchangeRate() throws ServiceException {
		EverydayExchangeRateDO everydayExchangeRateDo = everydayExchangeRateDao.getLastOne();
		if (everydayExchangeRateDo != null)
			return new ExchangeRateDTO(everydayExchangeRateDo.getZnzExchangeRate(),
					everydayExchangeRateDo.getUpdateTime());
		return null;
	}

}
