package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.KjDAO;
import org.zhinanzhen.b.dao.pojo.KjDO;
import org.zhinanzhen.b.service.KjService;
import org.zhinanzhen.b.service.KjStateEnum;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.b.service.pojo.KjDTO;
import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("KjService")
public class KjServiceImpl extends BaseService implements KjService {
	@Resource
	private KjDAO kjDao;
	@Resource
	private RegionDAO regionDao;

	@Override
	public int addKj(KjDTO kjDto) throws ServiceException {
		if (kjDto == null) {
			ServiceException se = new ServiceException("kjDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			KjDO kjDo = mapper.map(kjDto, KjDO.class);
			if (kjDto.getState() != null) {
				kjDo.setState(kjDto.getState().toString());
			}
			if (kjDao.addKj(kjDo) > 0) {
				kjDto.setId(kjDo.getId());
				return kjDo.getId();
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
	public int updateKj(KjDTO kjDto) throws ServiceException {
		if (kjDto == null) {
			ServiceException se = new ServiceException("kjDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			KjDO kjDo = mapper.map(kjDto, KjDO.class);
			if (kjDto.getState() != null) {
				kjDo.setState(kjDto.getState().toString());
			}
			return kjDao.updateKj(kjDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countKj(String name, Integer regionId) throws ServiceException {
		return kjDao.countKj(name, regionId);
	}

	@Override
	public List<KjDTO> listKj(String name, Integer regionId, int pageNum, int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<KjDTO> kjDtoList = new ArrayList<KjDTO>();
		List<KjDO> kjDoList = new ArrayList<KjDO>();
		try {
			kjDoList = kjDao.listKj(name, regionId, pageNum * pageSize, pageSize);
			if (kjDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (KjDO kjDo : kjDoList) {
			KjDTO kjDto = mapper.map(kjDo, KjDTO.class);
			if (StringUtil.isNotEmpty(kjDo.getState())) {
				kjDto.setState(KjStateEnum.get(kjDo.getState()));
			}
			RegionDO regionDo = regionDao.getRegionById(kjDo.getRegionId());
			if (regionDo != null) {
				kjDto.setRegionName(regionDo.getName());
				kjDto.setRegionDo(regionDo);
			}
			kjDtoList.add(kjDto);
		}
		return kjDtoList;
	}

	@Override
	public KjDTO getKjById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		KjDTO kjDto = null;
		try {
			KjDO kjDo = kjDao.getKjById(id);
			if (kjDo == null)
				return null;
			kjDto = mapper.map(kjDo, KjDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return kjDto;
	}
}