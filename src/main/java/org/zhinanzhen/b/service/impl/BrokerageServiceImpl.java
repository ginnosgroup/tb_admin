package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.BrokerageDAO;
import org.zhinanzhen.b.dao.pojo.BrokerageDO;
import org.zhinanzhen.b.service.BrokerageService;
import org.zhinanzhen.b.service.pojo.BrokerageDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("BrokerageService")
public class BrokerageServiceImpl extends BaseService implements BrokerageService {

	@Resource
	private BrokerageDAO brokerageDao;

	@Override
	public int addBrokerage(BrokerageDTO brokerageDto) throws ServiceException {
		if (brokerageDto == null) {
			ServiceException se = new ServiceException("brokerageDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			BrokerageDO brokerageDo = mapper.map(brokerageDto, BrokerageDO.class);
			if (brokerageDao.addBrokerage(brokerageDo) > 0) {
				return brokerageDo.getId();
			} else {
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateBrokerage(BrokerageDTO brokerageDto) throws ServiceException {
		if (brokerageDto == null) {
			ServiceException se = new ServiceException("brokerageDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			BrokerageDO brokerageDo = mapper.map(brokerageDto, BrokerageDO.class);
			return brokerageDao.updateBrokerage(brokerageDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countBrokerage(String stardDate, String endDate, Integer adviserId) throws ServiceException {
		return brokerageDao.countBrokerage(stardDate, endDate, adviserId);
	}

	@Override
	public List<BrokerageDTO> listBrokerage(String stardDate, String endDate, Integer adviserId, int pageNum,
			int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<BrokerageDTO> brokerageDtoList = new ArrayList<BrokerageDTO>();
		List<BrokerageDO> brokerageDoList = new ArrayList<BrokerageDO>();
		try {
			brokerageDoList = brokerageDao.listBrokerage(stardDate, endDate, adviserId, pageNum * pageSize, pageSize);
			if (brokerageDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (BrokerageDO brokerageDo : brokerageDoList) {
			BrokerageDTO brokerageDto = mapper.map(brokerageDo, BrokerageDTO.class);
			brokerageDtoList.add(brokerageDto);
		}
		return brokerageDtoList;
	}

	@Override
	public BrokerageDTO getBrokerageById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		BrokerageDTO brokerageDto = null;
		try {
			BrokerageDO brokerageDo = brokerageDao.getBrokerageById(id);
			if (brokerageDo == null) {
				return null;
			}
			brokerageDto = mapper.map(brokerageDo, BrokerageDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return brokerageDto;
	}

}
