package org.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.TagDAO;
import org.zhinanzhen.b.dao.pojo.TagDO;
import org.zhinanzhen.b.dao.pojo.UserTagDO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.AdviserStateEnum;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserAuthTypeEnum;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.service.pojo.TagDTO;
import org.zhinanzhen.tb.utils.Base64Util;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("userService")
public class UserServiceImpl extends BaseService implements UserService {
	@Resource
	private UserDAO userDao;
	@Resource
	private AdviserService adviserService;
	@Resource
	private TagDAO tagDao;

	@Override
	public int addUser(String name, String authNickname, Date birthday, String phone, String wechatUsername,
			String firstControllerContents, String visaCode, Date visaExpirationDate, String source, int adviserId)
			throws ServiceException {
		if (StringUtil.isEmpty(name)) {
			ServiceException se = new ServiceException("name is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isEmpty(authNickname)) {
			authNickname = name;
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
		return userDao.addUser(name, authNickname, birthday, phone, wechatUsername, firstControllerContents, visaCode,
				visaExpirationDate, source, adviserId);
	}

	@Override
	public int countUser(String name, UserAuthTypeEnum authType, String authNickname, String phone,
			String wechatUsername, int adviserId) throws ServiceException {
		if (authType == null) {
			return userDao.countUser(name, null, authNickname, phone, wechatUsername,
					adviserId <= 0 ? null : adviserId);
		} else {
			return userDao.countUser(name, authType.toString(), authNickname, phone, wechatUsername,
					adviserId <= 0 ? null : adviserId);
		}
	}

	@Override
	public int countUserByThisMonth(Integer adviserId) throws ServiceException {
		return userDao.countUserByThisMonth(adviserId);
	}

	@Override
	public List<UserDTO> listUser(String name, UserAuthTypeEnum authType, String authNickname, String phone,
			String wechatUsername, int adviserId, int pageNum, int pageSize) throws ServiceException {
		return listUser(name, authType, authNickname, phone, wechatUsername, adviserId, "gmt_create", true, pageNum,
				pageSize);
	}

	@Override
	public List<UserDTO> listUser(String name, UserAuthTypeEnum authType, String authNickname, String phone,
			String wechatUsername, int adviserId, String orderByField, Boolean isDesc, int pageNum, int pageSize)
			throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		if (StringUtil.isNotEmpty(orderByField))
			orderByField = orderByField.replace(";", ""); // 防注入
		else {
			orderByField = "gmt_create";
			isDesc = true;
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
				userDoList = userDao.listUser(name, null, authNickname, phone, wechatUsername,
						adviserId <= 0 ? null : adviserId, orderByField, isDesc, pageNum * pageSize, pageSize);
			} else {
				userDoList = userDao.listUser(name, authType.toString(), authNickname, phone, wechatUsername,
						adviserId <= 0 ? null : adviserId, orderByField, isDesc, pageNum * pageSize, pageSize);
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
			userDto.setTagList(listTagByUserId(userDto.getId()));
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
			userDto.setTagList(listTagByUserId(userDto.getId()));
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
		userDto.setTagList(listTagByUserId(userDto.getId()));
		return userDto;
	}

	@Override
	public boolean update(int id, String name, String authNickname, Date birthday, String phone, String wechatUsername,
			String firstControllerContents, String visaCode, Date visaExpirationDate, String source)
			throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isNotEmpty(phone) && userDao.countUser(null, null, null, phone, null, null) > 0) {
			List<UserDO> userList = userDao.listUser(null, null, null, phone, null, null, null, null, 0, 1);
			if (userList.size() > 0 && userList.get(0).getId() != id) { // 排除当前id
				ServiceException se = new ServiceException("The phone is already existed !");
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
		}
		return userDao.update(id, name, authNickname, birthday, phone, wechatUsername, firstControllerContents,
				visaCode, visaExpirationDate, source);
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
			userDto.setTagList(listTagByUserId(userDto.getId()));
			userDtoList.add(userDto);
		}
		return userDtoList;
	}

	@Override
	public int newTag(String name) throws ServiceException {
		TagDO tagDo = new TagDO();
		tagDo.setName(name);
		return tagDao.addTag(tagDo);
	}

	@Override
	public int addTag(int userId, int tagId) throws ServiceException {
		UserTagDO userTagDo = new UserTagDO();
		userTagDo.setUserId(userId);
		userTagDo.setTagId(tagId);
		return tagDao.addUserTag(userTagDo);
	}

	@Override
	public List<TagDTO> listTag() throws ServiceException {
		List<TagDTO> tagDtoList = new ArrayList<TagDTO>();
		List<TagDO> tagDoList = tagDao.listTag();
		tagDoList.forEach(tagDo -> {
			tagDtoList.add(mapper.map(tagDo, TagDTO.class));
		});
		return tagDtoList;
	}

	@Override
	public List<TagDTO> listTagByUserId(int userId) throws ServiceException {
		List<TagDTO> tagDtoList = new ArrayList<TagDTO>();
		List<TagDO> tagDoList = tagDao.listTagByUserId(userId);
		tagDoList.forEach(tagDo -> {
			tagDtoList.add(mapper.map(tagDo, TagDTO.class));
		});
		return tagDtoList;
	}

	@Override
	public TagDTO getTag(int tagId) throws ServiceException {
		return mapper.map(tagDao.getTagById(tagId), TagDTO.class);
	}

	@Override
	@Transactional
	public int deleteTagById(int id) throws ServiceException {
		tagDao.deleteUserTagByTagId(id);
		return tagDao.deleteTagById(id);
	}

	@Override
	public int deleteUserTagByUserId(int userId) throws ServiceException {
		return tagDao.deleteUserTagByUserId(userId);
	}

	@Override
	public int deleteUserTagByTagIdAndUserId(int tagId, int userId) throws ServiceException {
		return tagDao.deleteUserTagByTagIdAndUserId(tagId, userId);
	}

}
