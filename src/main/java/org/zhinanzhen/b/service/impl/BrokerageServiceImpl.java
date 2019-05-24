package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.BrokerageDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.ReceiveTypeDAO;
import org.zhinanzhen.b.dao.ServiceDAO;
import org.zhinanzhen.b.dao.pojo.BrokerageDO;
import org.zhinanzhen.b.dao.pojo.BrokerageListDO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ReceiveTypeDO;
import org.zhinanzhen.b.dao.pojo.ServiceDO;
import org.zhinanzhen.b.service.BrokerageService;
import org.zhinanzhen.b.service.pojo.BrokerageDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("BrokerageService")
public class BrokerageServiceImpl extends BaseService implements BrokerageService {

	@Resource
	private BrokerageDAO brokerageDao;

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private OfficialDAO officialDao;

	@Resource
	private ReceiveTypeDAO receiveTypeDao;

	@Resource
	private ServiceDAO serviceDao;

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
				brokerageDto.setId(brokerageDo.getId());
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
	public int countBrokerage(String keyword, String startHandlingDate, String endHandlingDate, String stardDate,
			String endDate, Integer adviserId, Integer userId) throws ServiceException {
		return brokerageDao.countBrokerage(keyword, startHandlingDate, endHandlingDate, stardDate, endDate, adviserId,
				userId);
	}

	@Override
	public List<BrokerageDTO> listBrokerage(String keyword, String startHandlingDate, String endHandlingDate,
			String stardDate, String endDate, Integer adviserId, Integer userId, int pageNum, int pageSize)
			throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<BrokerageDTO> brokerageDtoList = new ArrayList<>();
		List<BrokerageListDO> brokerageListDoList = new ArrayList<>();
		try {
			brokerageListDoList = brokerageDao.listBrokerage(keyword, startHandlingDate, endHandlingDate, stardDate,
					endDate, adviserId, userId, pageNum * pageSize, pageSize);
			if (brokerageListDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (BrokerageDO brokerageListDo : brokerageListDoList) {
			BrokerageDTO brokerageDto = mapper.map(brokerageListDo, BrokerageDTO.class);
			AdviserDO adviserDo = adviserDao.getAdviserById(brokerageListDo.getAdviserId());
			if (adviserDo != null) {
				brokerageDto.setAdviserName(adviserDo.getName());
			}
			OfficialDO officialDo = officialDao.getOfficialById(brokerageListDo.getOfficialId());
			if (officialDo != null) {
				brokerageDto.setOfficialName(officialDo.getName());
			}
			ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(brokerageListDo.getReceiveTypeId());
			if (receiveTypeDo != null) {
				brokerageDto.setReceiveTypeName(receiveTypeDo.getName());
			}
			ServiceDO serviceDo = serviceDao.getServiceById(brokerageListDo.getServiceId());
			if (serviceDo != null) {
				brokerageDto.setServiceCode(serviceDo.getCode());
			}
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

	@Override
	public int deleteBrokerageById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return brokerageDao.deleteBrokerageById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
