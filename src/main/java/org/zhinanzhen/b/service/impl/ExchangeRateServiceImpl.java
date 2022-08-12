package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.EverydayExchangeRateDAO;
import org.zhinanzhen.b.dao.pojo.EverydayExchangeRateDO;
import org.zhinanzhen.b.service.ExchangeRateService;
import org.zhinanzhen.b.service.pojo.ExchangeRateDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

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

	@Override
	public List<ExchangeRateDTO> listExchangeRate() throws ServiceException {
		List<EverydayExchangeRateDO> list = new ArrayList<>();
		List<ExchangeRateDTO> dtoList = new ArrayList<>();
		try {
			list = everydayExchangeRateDao.list();
			if (list == null)
				return null;
			list.forEach(v -> dtoList.add(mapper.map(v, ExchangeRateDTO.class)));
			return dtoList;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

}
