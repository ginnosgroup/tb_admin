package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.BrokerageDTO;

public interface BrokerageService {

	public int addBrokerage(BrokerageDTO brokerageDto);

	public int updateBrokerage(BrokerageDTO brokerageDto);

	public int countBrokerage(String stardDate, String endDate, Integer adviserId);

	public List<BrokerageDTO> listBrokerage(String stardDate, String endDate, Integer adviserId, int pageNum,
			int pageSize);

	public BrokerageDTO getBrokerageById(int id);

}
