package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.MaraDAO;
import org.zhinanzhen.b.dao.pojo.MaraDO;
import org.zhinanzhen.b.service.MaraService;
import org.zhinanzhen.b.service.MaraStateEnum;
import org.zhinanzhen.b.service.pojo.MaraDTO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("maraService")
public class MaraServiceImpl extends BaseService implements MaraService {

	@Resource
	private MaraDAO maraDao;
	@Resource
	private RegionDAO regionDao;

	@Override
	public int addMara(MaraDTO maraDto) throws ServiceException {
		if (maraDao == null) {
			ServiceException se = new ServiceException("maraDao is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			MaraDO maraDo = mapper.map(maraDto, MaraDO.class);
			if (maraDto.getState() != null) {
				maraDo.setState(maraDto.getState().toString());
			}
			if (maraDao.addMara(maraDo) > 0) {
				maraDto.setId(maraDo.getId());
				return maraDo.getId();
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
	public int updateMara(MaraDTO maraDto) throws ServiceException {
		if (maraDto == null) {
			ServiceException se = new ServiceException("maraDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			MaraDO maraDo = mapper.map(maraDto, MaraDO.class);
			if (maraDto.getState() != null) {
				maraDo.setState(maraDto.getState().toString());
			}
			return maraDao.updateMara(maraDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countMara(String name, Integer regionId) throws ServiceException {
		return maraDao.countMara(name, regionId);
	}

	@Override
	public List<MaraDTO> listMara(String name, Integer regionId, int pageNum, int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<MaraDTO> maraDtoList = new ArrayList<>();
		List<MaraDO> maraDoList = new ArrayList<>();
		try {
			maraDoList = maraDao.listMara(name, regionId, pageNum * pageSize, pageSize);
			if (maraDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (MaraDO maraDo : maraDoList) {
			MaraDTO maraDto = mapper.map(maraDo, MaraDTO.class);
			if (StringUtil.isNotEmpty(maraDo.getState())) {
				maraDto.setState(MaraStateEnum.get(maraDo.getState()));
			}
			RegionDO regionDo = regionDao.getRegionById(maraDo.getRegionId());
			if (regionDo != null) {
				maraDto.setRegionName(regionDo.getName());
				maraDto.setRegionDo(regionDo);
			}
			maraDtoList.add(maraDto);
		}
		return maraDtoList;
	}

	@Override
	public MaraDTO getMaraById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		MaraDTO maraDto = null;
		try {
			MaraDO maraDo = maraDao.getMaraById(id);
			if (maraDo == null) {
				return null;
			}
			maraDto = mapper.map(maraDo, MaraDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return maraDto;
	}

}
