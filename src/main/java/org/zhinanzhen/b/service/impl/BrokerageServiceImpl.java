package org.zhinanzhen.b.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.service.BrokerageService;
import org.zhinanzhen.b.service.pojo.BrokerageDTO;
import org.zhinanzhen.tb.service.impl.BaseService;

@Service("BrokerageService")
public class BrokerageServiceImpl extends BaseService implements BrokerageService {

	@Override
	public int addBrokerage(BrokerageDTO brokerageDto) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateBrokerage(BrokerageDTO brokerageDto) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int countBrokerage(String stardDate, String endDate, Integer adviserId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<BrokerageDTO> listBrokerage(String stardDate, String endDate, Integer adviserId, int pageNum,
			int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BrokerageDTO getBrokerageById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

}
