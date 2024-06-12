package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSONArray;
import org.aspectj.apache.bcel.generic.Tag;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.QywxExternalUserDAO;
import org.zhinanzhen.b.dao.pojo.QywxExternalUserDO;
import org.zhinanzhen.b.dao.pojo.QywxExternalUserDescriptionDO;
import org.zhinanzhen.b.service.QywxExternalUserService;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDTO;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDescriptionDTO;
import org.zhinanzhen.b.service.pojo.TagsDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

@Service("QywxExternalUserService")
public class QywxExternalUserServiceImpl extends BaseService implements QywxExternalUserService {

	@Resource
	private QywxExternalUserDAO qywxExternalUserDao;

	@Override
	public int add(QywxExternalUserDTO qywxExternalUserDto) throws ServiceException {
		if (qywxExternalUserDto == null) {
			ServiceException se = new ServiceException("qywxExternalUserDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			QywxExternalUserDO qywxExternalUserDo = mapper.map(qywxExternalUserDto, QywxExternalUserDO.class);
			if (qywxExternalUserDao.add(qywxExternalUserDo) > 0)
				return qywxExternalUserDo.getId();
			else
				return 0;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int update(QywxExternalUserDTO qywxExternalUserDto) throws ServiceException {
		if (qywxExternalUserDto == null) {
			ServiceException se = new ServiceException("qywxExternalUserDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			QywxExternalUserDO qywxExternalUserDo = mapper.map(qywxExternalUserDto, QywxExternalUserDO.class);
			return qywxExternalUserDao.update(qywxExternalUserDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int count(int adviserId, String state, String startDate, String endDate) throws ServiceException {
		return qywxExternalUserDao.count(adviserId, state, theDateTo00_00_00(startDate), theDateTo23_59_59(endDate));
	}

	@Override
	public List<QywxExternalUserDTO> list(int adviserId, String state, String startDate, String endDate, int pageNum,
			int pageSize) throws ServiceException {
		List<QywxExternalUserDTO> qywxExternalUserDtoList = new ArrayList<>();
		List<QywxExternalUserDO> qywxExternalUserDoList = null;
		try {
			qywxExternalUserDoList = qywxExternalUserDao.list(adviserId, state, theDateTo00_00_00(startDate),
					theDateTo23_59_59(endDate), pageNum * pageSize, pageSize);
			if (ObjectUtil.isNull(qywxExternalUserDoList))
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (QywxExternalUserDO qywxExternalUserDo : qywxExternalUserDoList) {
			String tags = qywxExternalUserDo.getTags();
			List<TagsDTO> tagsDTOS = JSONArray.parseArray(tags, TagsDTO.class);
			QywxExternalUserDTO qywxExternalUserDto = mapper.map(qywxExternalUserDo, QywxExternalUserDTO.class);
			qywxExternalUserDto.setTagsDTOS(tagsDTOS);
			qywxExternalUserDtoList.add(qywxExternalUserDto);
		}
		return qywxExternalUserDtoList;
	}

	@Override
	public QywxExternalUserDTO get(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		QywxExternalUserDO qywxExternalUserDo = null;
		try {
			qywxExternalUserDo = qywxExternalUserDao.get(id);
			if (ObjectUtil.isNull(qywxExternalUserDo))
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return mapper.map(qywxExternalUserDo, QywxExternalUserDTO.class);
	}

	@Override
	public QywxExternalUserDTO getByExternalUserid(String externalUserid) throws ServiceException {
		QywxExternalUserDO qywxExternalUserDo = null;
		try {
			qywxExternalUserDo = qywxExternalUserDao.getByExternalUserid(externalUserid);
			if (ObjectUtil.isNull(qywxExternalUserDo))
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return mapper.map(qywxExternalUserDo, QywxExternalUserDTO.class);
	}

	@Override
	public int addDesc(QywxExternalUserDescriptionDTO qywxExternalUserDescriptionDto) throws ServiceException {
		if (qywxExternalUserDescriptionDto == null) {
			ServiceException se = new ServiceException("qywxExternalUserDescriptionDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			QywxExternalUserDescriptionDO qywxExternalUserDescriptionDo = mapper.map(qywxExternalUserDescriptionDto,
					QywxExternalUserDescriptionDO.class);
			if (qywxExternalUserDao.addDesc(qywxExternalUserDescriptionDo) > 0)
				return qywxExternalUserDescriptionDo.getId();
			else
				return 0;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateDesc(QywxExternalUserDescriptionDTO qywxExternalUserDescriptionDto) throws ServiceException {
		if (qywxExternalUserDescriptionDto == null) {
			ServiceException se = new ServiceException("qywxExternalUserDescriptionDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			QywxExternalUserDescriptionDO qywxExternalUserDescriptionDo = mapper.map(qywxExternalUserDescriptionDto,
					QywxExternalUserDescriptionDO.class);
			return qywxExternalUserDao.updateDesc(qywxExternalUserDescriptionDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<QywxExternalUserDescriptionDTO> listDescByExternalUserid(String externalUserid, String key)
			throws ServiceException {
		if (StringUtil.isEmpty(externalUserid)) {
			ServiceException se = new ServiceException("externalUserid is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		List<QywxExternalUserDescriptionDTO> qywxExternalUserDescriptionDtoList = new ArrayList<>();
		List<QywxExternalUserDescriptionDO> qywxExternalUserDescriptionDoList = null;
		try {
			qywxExternalUserDescriptionDoList = qywxExternalUserDao.listDescByExternalUserid(externalUserid, key);
			if (ObjectUtil.isNull(qywxExternalUserDescriptionDoList))
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (QywxExternalUserDescriptionDO qywxExternalUserDescriptionDo : qywxExternalUserDescriptionDoList) {
			QywxExternalUserDescriptionDTO qywxExternalUserDescriptionDto = mapper.map(qywxExternalUserDescriptionDo,
					QywxExternalUserDescriptionDTO.class);
			qywxExternalUserDescriptionDtoList.add(qywxExternalUserDescriptionDto);
		}
		return qywxExternalUserDescriptionDtoList;
	}

	@Override
	public List<QywxExternalUserDescriptionDTO> listDescByApplicantId(int applicantId) throws ServiceException {
		if (applicantId <= 0) {
			ServiceException se = new ServiceException("applicantId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		List<QywxExternalUserDescriptionDTO> qywxExternalUserDescriptionDtoList = new ArrayList<>();
		List<QywxExternalUserDescriptionDO> qywxExternalUserDescriptionDoList = null;
		try {
			qywxExternalUserDescriptionDoList = qywxExternalUserDao.listDescByApplicantId(applicantId);
			if (ObjectUtil.isNull(qywxExternalUserDescriptionDoList))
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (QywxExternalUserDescriptionDO qywxExternalUserDescriptionDo : qywxExternalUserDescriptionDoList) {
			QywxExternalUserDescriptionDTO qywxExternalUserDescriptionDto = mapper.map(qywxExternalUserDescriptionDo,
					QywxExternalUserDescriptionDTO.class);
			qywxExternalUserDescriptionDtoList.add(qywxExternalUserDescriptionDto);
		}
		return qywxExternalUserDescriptionDtoList;
	}

}
