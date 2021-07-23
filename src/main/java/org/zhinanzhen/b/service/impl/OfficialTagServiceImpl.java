package org.zhinanzhen.b.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.OfficialTagDAO;
import org.zhinanzhen.b.dao.ServiceOrderOfficialTagDAO;
import org.zhinanzhen.b.dao.pojo.OfficialTagDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderOfficialTagDO;
import org.zhinanzhen.b.service.OfficialTagService;
import org.zhinanzhen.b.service.pojo.OfficialTagDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ListUtil;

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
	public List<OfficialTagDTO> list() throws ServiceException {
		List<OfficialTagDTO> officialTagDtoList = ListUtil.newArrayList();
		officialTagDao.listOfficialTag().forEach(ot -> officialTagDtoList.add(mapper.map(ot, OfficialTagDTO.class)));
		return officialTagDtoList;
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
	@Transactional(rollbackFor = ServiceException.class)
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
		OfficialTagDO officialTagDo = officialTagDao.getOfficialTagByServiceOrderId(serviceOrderId);
		if (officialTagDo != null && officialTagDo.getId() == id) {
			ServiceException se = new ServiceException("The tag is exists !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (ServiceOrderOfficialTagDao.countServiceOrderOfficialTag(id, serviceOrderId) > 0) {
			ServiceException se = new ServiceException("The tag is added !");
			se.setCode(ErrorCodeEnum.DATA_ERROR.code());
			throw se;
		}
		ServiceOrderOfficialTagDO serviceOrderOfficialTagDo = new ServiceOrderOfficialTagDO();
		serviceOrderOfficialTagDo.setServiceOrderId(serviceOrderId);
		serviceOrderOfficialTagDo.setOfficialTagId(id);
		return ServiceOrderOfficialTagDao.addServiceOrderOfficialTag(serviceOrderOfficialTagDo);
	}

	@Override
	public int updateServiceOrderOfficialTag(int id, int serviceOrderId) throws ServiceException {
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
		OfficialTagDO officialTagDo = officialTagDao.getOfficialTagByServiceOrderId(serviceOrderId);
		if (officialTagDo != null) {
			ServiceOrderOfficialTagDO serviceOrderOfficialTagDo = new ServiceOrderOfficialTagDO();
			serviceOrderOfficialTagDo.setServiceOrderId(serviceOrderId);
			serviceOrderOfficialTagDo.setOfficialTagId(id);
			return ServiceOrderOfficialTagDao.updateServiceOrderOfficialTag(serviceOrderOfficialTagDo);
		} else {
			ServiceException se = new ServiceException("The tag is not exists ! cannot update it.");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteServiceOrderOfficialTagByTagIdAndServiceOrderId(int id, int serviceOrderId)
			throws ServiceException {
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
		return ServiceOrderOfficialTagDao.deleteServiceOrderOfficialTagByTagIdAndServiceOrderId(id, serviceOrderId);
	}

}
