package org.zhinanzhen.b.dao;

import org.zhinanzhen.b.dao.pojo.EverydayExchangeRateDO;

public interface EverydayExchangeRateDAO {

	int add(EverydayExchangeRateDO everydayExchangeRateDo);

	EverydayExchangeRateDO getById(int id);

}
