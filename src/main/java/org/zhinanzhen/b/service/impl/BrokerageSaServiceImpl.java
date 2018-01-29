package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.BrokerageSaDAO;
import org.zhinanzhen.b.dao.pojo.BrokerageSaDO;
import org.zhinanzhen.b.dao.pojo.BrokerageSaListDO;
import org.zhinanzhen.b.service.BrokerageSaService;
import org.zhinanzhen.b.service.pojo.BrokerageSaDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("BrokerageSaService")
public class BrokerageSaServiceImpl extends BaseService implements BrokerageSaService {

	@Resource
	private BrokerageSaDAO brokerageSaDao;

	@Override
	public int addBrokerageSa(BrokerageSaDTO brokerageSaDto) throws ServiceException {
		if (brokerageSaDto == null) {
			ServiceException se = new ServiceException("brokerageSaDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			BrokerageSaDO brokerageSaDo = mapper.map(brokerageSaDto, BrokerageSaDO.class);
			if (brokerageSaDao.addBrokerageSa(brokerageSaDo) > 0) {
				return brokerageSaDo.getId();
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
	public int updateBrokerageSa(BrokerageSaDTO brokerageSaDto) throws ServiceException {
		if (brokerageSaDto == null) {
			ServiceException se = new ServiceException("brokerageSaDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			BrokerageSaDO brokerageSaDo = mapper.map(brokerageSaDto, BrokerageSaDO.class);
			return brokerageSaDao.updateBrokerageSa(brokerageSaDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countBrokerageSa(String keyword, String startHandlingDate,
			String endHandlingDate, , String startDate, String endDate, Integer adviserId, Integer schoolId) throws ServiceException {
		return brokerageSaDao.countBrokerageSa(keyword, startCreateDate, endCreateDate, startHandlingDate,
				endHandlingDate, adviserId, schoolId);
	}

	@Override
	public List<BrokerageSaDTO> listBrokerageSa(String keyword, String startCreateDate, String endCreateDate,
			String startHandlingDate, String endHandlingDate, Integer adviserId, Integer schoolId, int pageNum,
			int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<BrokerageSaDTO> brokerageSaDtoList = new ArrayList<>();
		List<BrokerageSaListDO> brokerageSaListDoList = new ArrayList<>();
		try {
			brokerageSaListDoList = brokerageSaDao.listBrokerageSa(keyword, startCreateDate, endCreateDate,
					startHandlingDate, endHandlingDate, adviserId, schoolId, pageNum * pageSize, pageSize);
			if (brokerageSaListDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (BrokerageSaListDO brokerageSaListDo : brokerageSaListDoList) {
			BrokerageSaDTO brokerageSaDto = mapper.map(brokerageSaListDo, BrokerageSaDTO.class);
			brokerageSaDtoList.add(brokerageSaDto);
		}
		return brokerageSaDtoList;
	}

	@Override
	public BrokerageSaDTO getBrokerageSaById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		BrokerageSaDTO brokerageSaDto = null;
		try {
			BrokerageSaDO brokerageSaDo = brokerageSaDao.getBrokerageSaById(id);
			if (brokerageSaDo == null) {
				return null;
			}
			brokerageSaDto = mapper.map(brokerageSaDo, BrokerageSaDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return brokerageSaDto;
	}

	@Override
	public int deleteBrokerageSaById(int id) throws ServiceException {
		return brokerageSaDao.deleteBrokerageSaById(id);
	}

}
