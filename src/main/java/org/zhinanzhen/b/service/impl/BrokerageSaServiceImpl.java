package org.zhinanzhen.b.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.service.BrokerageSaService;
import org.zhinanzhen.b.service.pojo.BrokerageSaDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

@Service("BrokerageSaService")
public class BrokerageSaServiceImpl extends BaseService implements BrokerageSaService {

	@Override
	public int addBrokerageSa(BrokerageSaDTO brokerageSaDto) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateBrokerageSa(BrokerageSaDTO brokerageSaDto) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int countBrokerageSa(String keyword, String startCreateDate, String endCreateDate, String startHandlingDate,
			String endHandlingDate, Integer adviserId, Integer schoolId) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<BrokerageSaDTO> listBrokerageSa(String keyword, String startCreateDate, String endCreateDate,
			String startHandlingDate, String endHandlingDate, Integer adviserId, Integer schoolId, int pageNum,
			int pageSize) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BrokerageSaDTO getBrokerageSaById(int id) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int deleteBrokerageSaById(int id) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

}
