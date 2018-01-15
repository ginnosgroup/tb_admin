package org.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.AdviserStateEnum;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserAuthTypeEnum;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.Base64Util;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("userService")
public class UserServiceImpl extends BaseService implements UserService {
	@Resource
	private UserDAO userDao;
	@Resource
	private AdviserService adviserService;

	@Override
	public int addUser(String name, Date birthday, String phone, int adviserId) throws ServiceException {
		if (StringUtil.isEmpty(name)) {
			ServiceException se = new ServiceException("name is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (birthday == null) {
			ServiceException se = new ServiceException("birthday is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (phone == null) {
			ServiceException se = new ServiceException("phone is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (adviserId <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		return userDao.addUser(name, formatter.format(birthday), phone, adviserId);
	}

	@Override
	public int countUser(String name, UserAuthTypeEnum authType, String authNickname, String phone)
			throws ServiceException {
		if (authType == null) {
			return userDao.countUser(name, null, authNickname, phone);
		} else {
			return userDao.countUser(name, authType.toString(), authNickname, phone);
		}
	}

	@Override
	public List<UserDTO> listUser(String name, UserAuthTypeEnum authType, String authNickname, String phone,
			int pageNum, int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<UserDTO> userDtoList = new ArrayList<UserDTO>();
		List<UserDO> userDoList = new ArrayList<UserDO>();

		try {
			authNickname = new String(Base64Util.encodeBase64(authNickname.getBytes()));
		} catch (Exception e) {
			System.out.println(("昵称转码失败"));
		}
		try {
			if (authType == null) {
				userDoList = userDao.listUser(name, null, authNickname, phone, pageNum * pageSize, pageSize);
			} else {
				userDoList = userDao.listUser(name, authType.toString(), authNickname, phone, pageNum * pageSize,
						pageSize);
			}
			if (userDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (UserDO userDo : userDoList) {
			UserDTO userDto = mapper.map(userDo, UserDTO.class);
			if (userDto.getAdviserId() > 0) {
				AdviserDTO adviserDto = adviserService.getAdviserById(userDto.getAdviserId());
				userDto.setAdviserDto(adviserDto);
			}
			if (userDto.getRecommendOpenid() != null) {
				UserDTO recommendUserDto = getUserByOpenId(UserAuthTypeEnum.WECHAT.toString(),
						userDto.getRecommendOpenid());
				userDto.setRecommendUserDto(recommendUserDto);
			}
			try {
				userDto.setAuthNickname(new String(Base64Util.decodeBase64(userDto.getAuthNickname()), "utf-8"));
			} catch (Exception e) {
				System.out.println(("昵称转码失败 userId = " + userDto.getId()));
			}
			userDtoList.add(userDto);
		}
		return userDtoList;
	}

	@Override
	public UserDTO getUserById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		UserDTO userDto = null;
		try {
			UserDO userDo = userDao.getUserById(id);
			if (userDo == null) {
				ServiceException se = new ServiceException("the user is't exist .userId = " + id);
				se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
				throw se;
			}
			userDto = mapper.map(userDo, UserDTO.class);
			if (userDto.getAdviserId() > 0) {
				AdviserDTO adviserDto = adviserService.getAdviserById(userDto.getAdviserId());
				userDto.setAdviserDto(adviserDto);
			}
			if (userDto.getRecommendOpenid() != null) {
				UserDTO recommendUserDto = getUserByOpenId(UserAuthTypeEnum.WECHAT.toString(),
						userDto.getRecommendOpenid());
				userDto.setRecommendUserDto(recommendUserDto);
			}
			System.out.println("old:" + userDto.getAuthNickname());
			try {
				userDto.setAuthNickname(new String(Base64Util.decodeBase64(userDto.getAuthNickname()), "utf-8"));
			} catch (Exception e) {
				System.out.println(("昵称转码失败 userId = " + userDto.getId()));
			}
			System.out.println("new:" + userDto.getAuthNickname());
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return userDto;
	}

	@Override
	public UserDTO getUserByOpenId(String thirdType, String thirdId) throws ServiceException {
		if (StringUtil.isEmpty(thirdType) || UserAuthTypeEnum.get(thirdType) == null) {
			ServiceException se = new ServiceException("thirdType ERROR ! thirdType = " + thirdType);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isEmpty(thirdId)) {
			ServiceException se = new ServiceException("thirdId is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		UserDO userDo = userDao.getUserByThird(thirdType, thirdId);
		UserDTO userDto = mapper.map(userDo, UserDTO.class);
		if (userDto.getAdviserId() > 0) {
			AdviserDTO adviserDto = adviserService.getAdviserById(userDto.getAdviserId());
			userDto.setAdviserDto(adviserDto);
		}
		return userDto;
	}

	@Override
	public boolean updateAdviserId(int adviserId, int id) throws ServiceException {
		if (adviserId <= 0) {
			ServiceException se = new ServiceException("adviserId error ! adviserId = " + adviserId);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (id <= 0) {
			ServiceException se = new ServiceException("userId error ! userId = " + id);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		UserDTO userDto = getUserById(id);
		if (userDto == null) {
			ServiceException se = new ServiceException("userDto not found ! userId = " + id);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		AdviserDTO adviserDto = adviserService.getAdviserById(adviserId);
		if (adviserDto == null) {
			ServiceException se = new ServiceException("adviserDto not found ! adviserId = " + adviserId);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (!AdviserStateEnum.ENABLED.equals(adviserDto.getState())) {
			ServiceException se = new ServiceException("adviserDto not ENABLED ! adviserId = " + adviserId);
			se.setCode(ErrorCodeEnum.DATA_ERROR.code());
			throw se;
		}
		return userDao.updateAdviserById(adviserId, id);
	}

	@Override
	public List<UserDTO> listUserByRecommendOpenId(String recommendOpenId) throws ServiceException {
		if (StringUtil.isEmpty(recommendOpenId)) {
			ServiceException se = new ServiceException("recommendOpenId is null");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		List<UserDTO> userDtoList = new ArrayList<UserDTO>();
		List<UserDO> userDoList = new ArrayList<UserDO>();

		try {
			userDoList = userDao.listUserByRecommendOpenId(recommendOpenId);
			if (userDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (UserDO userDo : userDoList) {
			UserDTO userDto = mapper.map(userDo, UserDTO.class);
			if (userDto.getAdviserId() > 0) {
				AdviserDTO adviserDto = adviserService.getAdviserById(userDto.getAdviserId());
				userDto.setAdviserDto(adviserDto);
			}
			if (userDto.getRecommendOpenid() != null) {
				UserDTO recommendUserDto = getUserByOpenId(UserAuthTypeEnum.WECHAT.toString(),
						userDto.getRecommendOpenid());
				userDto.setRecommendUserDto(recommendUserDto);
			}
			try {
				userDto.setAuthNickname(new String(Base64Util.decodeBase64(userDto.getAuthNickname()), "utf-8"));
			} catch (Exception e) {
				System.out.println(("昵称转码失败 userId = " + userDto.getId()));
			}
			userDtoList.add(userDto);
		}
		return userDtoList;
	}

}
