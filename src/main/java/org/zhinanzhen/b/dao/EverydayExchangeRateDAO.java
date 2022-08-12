package org.zhinanzhen.b.dao;

import java.util.List;

import org.zhinanzhen.b.dao.pojo.EverydayExchangeRateDO;

public interface EverydayExchangeRateDAO {

	int add(EverydayExchangeRateDO everydayExchangeRateDo);

	EverydayExchangeRateDO getLastOne();
	
	List<EverydayExchangeRateDO> list();

}
