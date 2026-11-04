package org.zhinanzhen.tb.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.KjDAO;
import org.zhinanzhen.b.dao.MaraDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.pojo.KjDO;
import org.zhinanzhen.b.dao.pojo.MaraDO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
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

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private MaraDAO maraDao;

	@Resource
	private OfficialDAO officialDao;

	@Resource
	private KjDAO kjDao;

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
			int isAdded = adminUserDao.add(adminUserDo);
			if (isAdded > 0) {
				if (StringUtil.equals(apList, "GW"))
					sendMail(username, "佣金系统帐号已经开通",
							StringUtil.merge("Hi 顾问老师: <br/>您的佣金系统帐号已经开通．<br/>登录网址:https://yongjinbiao.zhinanzhen.org/webroot_new/welcome<br/>登录帐号:",
									username, "<br/>登录密码:", password, "请查看顾问佣金系统培训资料:https://doc.weixin.qq.com/doc/w2_AEYAdQbUANwQps2udu8TPiXRT7a8C?scode=ALMA7AfIAA4JFhNyzpAEYAdQbUANw&version=4.1.36.6011&platform=win"));
				if (StringUtil.equals(apList, "WA"))
					sendMail(username, "佣金系统帐号已经开通", StringUtil.merge(
							"Hi 文案老师: <br/>您的佣金系统帐号已经开通．<br/>登录网址:https://yongjinbiao.zhinanzhen.org/webroot_new/welcome<br/>登录帐号是你的企业邮箱，登录密码:",
							password,
							"<br/>如何使用文案佣金系统请看下方培训视频．<br/>https://doc.weixin.qq.com/doc/w3_AEYAdQbUANwhAaFBC7wTRKX09hE0E"));
			}
				
			return isAdded;
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
				adminUserDo = adminUserDao.getAdminUserByUsername(username);
				if (adminUserDo != null && "DISABLED".equalsIgnoreCase(adminUserDo.getStatus())) // 判断帐号是否禁用
					return -1;
				return 0;
			}
			if (!areLoginRolesEnabled(adminUserDo))
				return -1;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return adminUserDo.getId();
	}

	private boolean areLoginRolesEnabled(AdminUserDO adminUser) {
		String apList = adminUser.getApList();
		if (apList == null)
			return true;
		// 按实际登录角色校验；多角色账号必须全部启用。
		if (apList.contains("GW")) {
			Integer adviserId = adminUser.getAdviserId();
			if (adviserId == null || adviserId <= 0)
				return false;
			AdviserDO adviser = adviserDao.getAdviserById(adviserId);
			if (adviser == null || !"ENABLED".equalsIgnoreCase(adviser.getState()))
				return false;
		}
		if (apList.contains("MA")) {
			Integer maraId = adminUser.getMaraId();
			if (maraId == null || maraId <= 0)
				return false;
			MaraDO mara = maraDao.getMaraById(maraId);
			if (mara == null || !"ENABLED".equalsIgnoreCase(mara.getState()))
				return false;
		}
		if (apList.contains("WA")) {
			Integer officialId = adminUser.getOfficialId();
			if (officialId == null || officialId <= 0)
				return false;
			OfficialDO official = officialDao.getOfficialById(officialId);
			if (official == null || !"ENABLED".equalsIgnoreCase(official.getState()))
				return false;
		}
		if (apList.contains("KJ")) {
			Integer kjId = adminUser.getKjId();
			if (kjId == null || kjId <= 0)
				return false;
			KjDO kj = kjDao.getKjById(kjId);
			if (kj == null || !"ENABLED".equalsIgnoreCase(kj.getState()))
				return false;
		}
		return true;
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
