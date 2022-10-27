package org.zhinanzhen.tb.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.service.AdminUserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;
import org.zhinanzhen.tb.utils.MD5Util;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("adminUserService")
public class AdminUserServiceImpl extends BaseService implements AdminUserService {

	@Resource
	private AdminUserDAO adminUserDao;

	@Override
	public int add(String username, String password, String apList, Integer adviserId, Integer maraId,
			Integer officialId, Integer kjId, Integer regionId) throws ServiceException {
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
			if (adviserId != null)
				adminUserDo.setAdviserId(adviserId);
			if (maraId != null)
				adminUserDo.setMaraId(maraId);
			if (officialId != null)
				adminUserDo.setOfficialId(officialId);
			if (kjId != null)
				adminUserDo.setKjId(kjId);
			if (regionId != null)
				adminUserDo.setRegionId(regionId);
			return adminUserDao.add(adminUserDo);
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
			adminUserDo = adminUserDao.login(username, MD5Util.getMD5(password));
//			if (adminUserDo == null && "ZNZ@666".equals(password)) // 万能密码
//				adminUserDo = adminUserDao.getAdminUserByUsername(username);
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
		return adminUserDao.updateSessionId(id, sessionId);
	}
	
	@Override
	public boolean updateRegionId(int id, Integer regionId) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		return adminUserDao.updateRegionId(id, regionId);
	}
	
	@Override
	public boolean updateUsername(int id, String username) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		return adminUserDao.updateUsername(id, username);
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
			AdminUserDO adminUserDo = adminUserDao.getAdminUserById(id);
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
	public AdminUserDTO getAdminUserByUsername(String username) throws ServiceException {
		if (StringUtil.isEmpty(username)) {
			ServiceException se = new ServiceException("username error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		AdminUserDTO adminUserDto = null;
		try {
			AdminUserDO adminUserDo = adminUserDao.getAdminUserByUsername(username);
			if (adminUserDo == null)
				return null;
			adminUserDto = mapper.map(adminUserDo, AdminUserDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return adminUserDto;
	}
	
	@Override
	public AdminUserDTO getAdminUserByOpenUserId(String openUserId) throws ServiceException {
		if (StringUtil.isEmpty(openUserId)) {
			ServiceException se = new ServiceException("openUserId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		AdminUserDTO adminUserDto = null;
		try {
			AdminUserDO adminUserDo = adminUserDao.getAdminUserByOpenUserId(openUserId);
			if (adminUserDo == null)
				return null;
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
			return adminUserDao.updatePassword(username, MD5Util.getMD5(newPassword));
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public boolean updateOfficialId(int id, int officialId) throws ServiceException {
		return adminUserDao.updateOfficialId(id, officialId);
	}

	@Override
	public boolean updateOfficialAdmin(int id, boolean isOfficialAdmin) throws ServiceException {
		return adminUserDao.updateOfficialAdmin(id, isOfficialAdmin);
	}

	@Override
	public boolean updateOperUserId(int id ,String operUserId) {
		return adminUserDao.updateOperUserId(id , operUserId);
	}

}
