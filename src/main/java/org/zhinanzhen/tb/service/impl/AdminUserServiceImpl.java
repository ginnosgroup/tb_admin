package org.zhinanzhen.tb.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.service.AdminUserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;
import org.zhinanzhen.tb.utils.MD5Util;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("adminUserService")
public class AdminUserServiceImpl extends BaseService implements AdminUserService {

	@Resource
	private AdminUserDAO adminUserDAO;

	@Override
	public int add(String username, String password, String apList, int adviserId) throws ServiceException {
		if (StringUtil.isEmpty(password)) {
			ServiceException se = new ServiceException("password is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			AdminUserDO adminUserDo = new AdminUserDO();
			adminUserDo.setUsername(username);
			adminUserDo.setPassword(MD5Util.getMD5(password));
			adminUserDo.setApList(apList);
			adminUserDo.setAdviserId(adviserId);
			return adminUserDAO.add(adminUserDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int login(String username, String password) throws ServiceException {
		if (StringUtil.isEmpty(username)) {
			ServiceException se = new ServiceException("username is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isEmpty(password)) {
			ServiceException se = new ServiceException("password is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		AdminUserDO adminUserDo = null;
		try {
			adminUserDo = adminUserDAO.login(username, MD5Util.getMD5(password));
			if (adminUserDo == null) {
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return adminUserDo.getId();
	}

	@Override
	public boolean updateSessionId(int id, String sessionId) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		return adminUserDAO.updateSessionId(id, sessionId);
	}

	@Override
	public AdminUserDTO getAdminUserById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		AdminUserDTO adminUserDto = null;
		try {
			AdminUserDO adminUserDo = adminUserDAO.getAdminUserById(id);
			if (adminUserDo == null) {
				ServiceException se = new ServiceException("the administrator is't exist .");
				se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
				throw se;
			}
			adminUserDto = mapper.map(adminUserDo, AdminUserDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return adminUserDto;
	}

	@Override
	public boolean updatePassword(String username, String newPassword) throws ServiceException {
		if (StringUtil.isEmpty(username)) {
			ServiceException se = new ServiceException("username is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isEmpty(newPassword)) {
			ServiceException se = new ServiceException("newPassword is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return adminUserDAO.updatePassword(username, MD5Util.getMD5(newPassword));
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
