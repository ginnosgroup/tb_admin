package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.QywxExternalUserDAO;
import org.zhinanzhen.b.dao.pojo.QywxExternalUserDO;
import org.zhinanzhen.b.service.QywxExternalUserService;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ObjectUtil;

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
	public int count(int adviserId, String state, String startDate, String endDate) throws ServiceException {
		return qywxExternalUserDao.count(adviserId, state, startDate, endDate);
	}

	@Override
	public List<QywxExternalUserDTO> list(int adviserId, String state, String startDate, String endDate, int pageNum,
			int pageSize) throws ServiceException {
		List<QywxExternalUserDTO> qywxExternalUserDtoList = new ArrayList<>();
		List<QywxExternalUserDO> qywxExternalUserDoList = null;
		try {
			qywxExternalUserDoList = qywxExternalUserDao.list(adviserId, state, startDate, endDate, pageNum * pageSize,
					pageSize);
			if (ObjectUtil.isNull(qywxExternalUserDoList))
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (QywxExternalUserDO qywxExternalUserDo : qywxExternalUserDoList) {
			QywxExternalUserDTO qywxExternalUserDto = mapper.map(qywxExternalUserDo, QywxExternalUserDTO.class);
			qywxExternalUserDtoList.add(qywxExternalUserDto);
		}
		return qywxExternalUserDtoList;
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

}
