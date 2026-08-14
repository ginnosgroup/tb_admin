package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.PortalDAO;
import org.zhinanzhen.b.dao.pojo.PortalDO;
import org.zhinanzhen.b.service.PortalService;
import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("PortalService")
public class PortalServiceImpl extends BaseService implements PortalService {

	@Resource
	private PortalDAO portalDao;

	@Override
	public int addPortal(PortalDTO portalDto) throws ServiceException {
		if (portalDto == null) {
			ServiceException se = new ServiceException("portalDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalDO portalDo = mapper.map(portalDto, PortalDO.class);
			if (portalDao.addPortal(portalDo) > 0) {
				portalDto.setId(portalDo.getId());
				return portalDo.getId();
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
	public int updatePortal(PortalDTO portalDto) throws ServiceException {
		if (portalDto == null) {
			ServiceException se = new ServiceException("portalDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalDO portalDo = mapper.map(portalDto, PortalDO.class);
			return portalDao.updatePortal(portalDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<PortalDTO> listPortal(Integer typeId, String strState, String keyword, int pageNum, int pageSize)
			throws ServiceException {
		List<PortalDTO> portalDtoList = new ArrayList<PortalDTO>();
		List<PortalDO> portalDoList = new ArrayList<PortalDO>();
		try {
			portalDoList = portalDao.listPortal(typeId, strState, keyword, pageNum * pageSize, pageSize);
			if (portalDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (PortalDO portalDo : portalDoList) {
			PortalDTO portalDto = mapper.map(portalDo, PortalDTO.class);
			portalDtoList.add(portalDto);
		}
		return portalDtoList;
	}

	@Override
	public int countPortal(Integer typeId, String strState, String keyword) throws ServiceException {
		try {
			return portalDao.countPortal(typeId, strState, keyword);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public PortalDTO getPortal(Integer id) throws ServiceException {
		if (id == null || id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalDO portalDo = portalDao.getPortalById(id);
			if (portalDo == null) {
				ServiceException se = new ServiceException("No data !");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
			return mapper.map(portalDo, PortalDTO.class);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deletePortal(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalDao.deletePortal(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
