package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.PortalTypeDAO;
import org.zhinanzhen.b.dao.pojo.PortalTypeDO;
import org.zhinanzhen.b.service.PortalTypeService;
import org.zhinanzhen.b.service.pojo.PortalTypeDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("PortalTypeService")
public class PortalTypeServiceImpl extends BaseService implements PortalTypeService {

	@Resource
	private PortalTypeDAO portalTypeDao;

	@Override
	public int addPortalType(PortalTypeDTO portalTypeDto) throws ServiceException {
		if (portalTypeDto == null) {
			ServiceException se = new ServiceException("portalTypeDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalTypeDO portalTypeDo = mapper.map(portalTypeDto, PortalTypeDO.class);
			if (portalTypeDao.addPortalType(portalTypeDo) > 0) {
				portalTypeDto.setId(portalTypeDo.getId());
				return portalTypeDo.getId();
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
	public int updatePortalType(PortalTypeDTO portalTypeDto) throws ServiceException {
		if (portalTypeDto == null) {
			ServiceException se = new ServiceException("portalTypeDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalTypeDO portalTypeDo = mapper.map(portalTypeDto, PortalTypeDO.class);
			return portalTypeDao.updatePortalType(portalTypeDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<PortalTypeDTO> listPortalType(Integer isDelete, String keyword) throws ServiceException {
		List<PortalTypeDTO> portalTypeDtoList = new ArrayList<PortalTypeDTO>();
		List<PortalTypeDO> portalTypeDoList = new ArrayList<PortalTypeDO>();
		try {
			portalTypeDoList = portalTypeDao.listPortalType(isDelete, keyword);
			if (portalTypeDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (PortalTypeDO portalTypeDo : portalTypeDoList) {
			PortalTypeDTO portalTypeDto = mapper.map(portalTypeDo, PortalTypeDTO.class);
			portalTypeDtoList.add(portalTypeDto);
		}
		return portalTypeDtoList;
	}

	@Override
	public PortalTypeDTO getPortalType(Integer id) throws ServiceException {
		if (id == null || id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalTypeDO portalTypeDo = portalTypeDao.getPortalTypeById(id);
			if (portalTypeDo == null) {
				ServiceException se = new ServiceException("No data !");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
			return mapper.map(portalTypeDo, PortalTypeDTO.class);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deletePortalType(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalTypeDao.deletePortalType(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
