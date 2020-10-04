package org.zhinanzhen.b.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.OfficialTagDAO;
import org.zhinanzhen.b.dao.ServiceOrderOfficialTagDAO;
import org.zhinanzhen.b.dao.pojo.OfficialTagDO;
import org.zhinanzhen.b.service.OfficialTagService;
import org.zhinanzhen.b.service.pojo.OfficialTagDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("OfficialTagService")
public class OfficialTagServiceImpl extends BaseService implements OfficialTagService {

	@Resource
	private OfficialTagDAO officialTagDao;

	@Resource
	ServiceOrderOfficialTagDAO ServiceOrderOfficialTagDao;

	@Override
	public int add(OfficialTagDTO officialTagDto) throws ServiceException {
		if (officialTagDto == null) {
			ServiceException se = new ServiceException("officialTagDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		OfficialTagDO officialTagDo = mapper.map(officialTagDto, OfficialTagDO.class);
		return officialTagDao.addOfficialTag(officialTagDo) > 0 ? officialTagDo.getId() : 0;
	}

	@Override
	public int update(OfficialTagDTO officialTagDto) throws ServiceException {
		if (officialTagDto == null) {
			ServiceException se = new ServiceException("officialTagDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		OfficialTagDO officialTagDo = mapper.map(officialTagDto, OfficialTagDO.class);
		return officialTagDao.updateOfficialTag(officialTagDo) > 0 ? officialTagDo.getId() : 0;
	}

	@Override
	public OfficialTagDTO get(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		OfficialTagDTO officialTagDto = null;
		try {
			OfficialTagDO officialTagDo = officialTagDao.getOfficialTagById(id);
			if (officialTagDo == null)
				return null;
			officialTagDto = mapper.map(officialTagDo, OfficialTagDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return officialTagDto;
	}

	@Override
	@Transactional
	public int delete(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		ServiceOrderOfficialTagDao.deleteServiceOrderOfficialTagByOfficialTagId(id);
		return officialTagDao.deleteOfficialTagById(id);
	}

	@Override
	public int addServiceOrderOfficialTag(int id, int serviceOrderId) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (serviceOrderId <= 0) {
			ServiceException se = new ServiceException("serviceOrderId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		return ServiceOrderOfficialTagDao.addServiceOrderOfficialTag(id, serviceOrderId) > 0 ? id : 0;
	}

	@Override
	public int deleteServiceOrderOfficialTagById(int serviceOrderOfficialTagId) throws ServiceException {
		if (serviceOrderOfficialTagId <= 0) {
			ServiceException se = new ServiceException("serviceOrderOfficialTagId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		return ServiceOrderOfficialTagDao.deleteServiceOrderOfficialTagById(serviceOrderOfficialTagId);
	}

}
