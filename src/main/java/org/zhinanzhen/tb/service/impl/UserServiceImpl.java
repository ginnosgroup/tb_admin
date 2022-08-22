package org.zhinanzhen.tb.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.ApplicantDAO;
import org.zhinanzhen.b.dao.MaraDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.TagDAO;
import org.zhinanzhen.b.dao.pojo.ApplicantDO;
import org.zhinanzhen.b.dao.pojo.MaraDO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.dao.pojo.TagDO;
import org.zhinanzhen.b.dao.pojo.UserTagDO;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserAdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.AdviserStateEnum;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserAuthTypeEnum;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.service.pojo.TagDTO;
import org.zhinanzhen.tb.service.pojo.UserAdviserDTO;
import org.zhinanzhen.tb.utils.Base64Util;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

@Service("userService")
public class UserServiceImpl extends BaseService implements UserService {
	@Resource
	private UserDAO userDao;
	@Resource
	private AdviserDAO adviserDao;
	@Resource
	private ApplicantDAO applicantDao;
	@Resource
	private TagDAO tagDao;
	@Resource
	private ServiceOrderDAO serviceOrderDao;
	@Resource
	private MaraDAO maraDao;
	@Resource
	private OfficialDAO officialDao;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public int addUser(String name, String authNickname, Date birthday, String areaCode, String phone, String email,
			String wechatUsername, String firstControllerContents, String visaCode, Date visaExpirationDate,
			String source, int adviserId, int regionId) throws ServiceException {
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
		UserDO userDo = new UserDO();
		userDo.setName(name);
		userDo.setAuthNickname(authNickname);
		userDo.setAuthType("BROKERAGE");
		userDo.setAuthOpenid("");
		userDo.setBirthday(birthday);
		userDo.setAreaCode(areaCode);
		userDo.setPhone(phone);
		userDo.setEmail(email);
		userDo.setWechatUsername(wechatUsername);
		userDo.setFirstControllerContents(firstControllerContents);
		userDo.setVisaCode(visaCode);
		userDo.setVisaExpirationDate(visaExpirationDate);
		userDo.setSource(source);
		userDo.setAdviserId(adviserId);
		if (regionId > 0)
			userDo.setRegionId(regionId);
		else {
			AdviserDO adviserDo = adviserDao.getAdviserById(adviserId);
			if (adviserDo != null)
				userDo.setRegionId(adviserDo.getRegionId());
			else {
				ServiceException se = new ServiceException("The adviser is not exist : " + adviserId);
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
		}
		if (userDao.addUser(userDo) > 0) {
			userDao.addUserAdviser(userDo.getId(), adviserId, true);
			return userDo.getId();
		} else
			return 0;
	}

	@Override
	public int addUserAdviser(int userId, int adviserId) throws ServiceException {
		List<UserAdviserDO> userAdviserList = userDao.listUserAdviserByUserId(userId);
		if (userAdviserList == null || userAdviserList.size() == 0)
			return userDao.addUserAdviser(userId, adviserId, true);
		else
			for (UserAdviserDO userAdviserDo : userAdviserList) {
				if (userAdviserDo.getAdviserId() == adviserId) {
					UserDO userDo = userDao.getUserById(userId);
					AdviserDO adviserDo = adviserDao.getAdviserById(adviserId);
					if (userDo != null && adviserDo != null) {
						ServiceException se = new ServiceException("用户" + userDo.getName() + "(" + userId + ")已属于顾问"
								+ adviserDo.getName() + "(" + adviserId + ")!");
						se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
						throw se;
					} else {
						ServiceException se = new ServiceException("参数错误!");
						se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
						throw se;
					}
				}
			}
		return userDao.addUserAdviser(userId, adviserId, false);
	}

	@Override
	public int countUser(String name, UserAuthTypeEnum authType, String authNickname, String phone, String areaCode,
			String wechatUsername, int adviserId, String applicantName, List<Integer> regionIdList, Integer tagId)
			throws ServiceException {
		if (authType == null) {
			return userDao.countUser(name, null, authNickname, phone, areaCode, wechatUsername,
					adviserId <= 0 ? null : adviserId, applicantName, regionIdList, tagId);
		} else {
			return userDao.countUser(name, authType.toString(), authNickname, phone, areaCode, wechatUsername,
					adviserId <= 0 ? null : adviserId, applicantName, regionIdList, tagId);
		}
	}

	@Override
	public int countUserByThisMonth(Integer adviserId, List<Integer> regionIdList) throws ServiceException {
		return userDao.countUserByThisMonth(adviserId, regionIdList);
	}

	@Override
	public List<UserDTO> listUser(String name, UserAuthTypeEnum authType, String authNickname, String phone,
			String areaCode, String email, String wechatUsername, int adviserId, String applicantName,
			List<Integer> regionIdList, int pageNum, int pageSize) throws ServiceException {
		return listUser(name, authType, authNickname, phone, areaCode, email, wechatUsername, adviserId, applicantName,
				regionIdList, null, "gmt_create", true, pageNum, pageSize);
	}

	@Override
	public List<UserDTO> listUser(String name, UserAuthTypeEnum authType, String authNickname, String phone, String areaCode,
			String email, String wechatUsername, int adviserId, String applicantName, List<Integer> regionIdList,
			Integer tagId, String orderByField, Boolean isDesc, int pageNum, int pageSize) throws ServiceException {
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
//			System.out.println(("昵称转码失败"));
		}
		try {
			if (authType == null) {
				userDoList = userDao.listUser(name, null, authNickname, phone, areaCode, email, wechatUsername,
						adviserId <= 0 ? null : adviserId, applicantName, regionIdList, tagId, orderByField, isDesc,
						pageNum * pageSize, pageSize);
			} else {
				userDoList = userDao.listUser(name, authType.toString(), authNickname, phone, areaCode, email, wechatUsername,
						adviserId <= 0 ? null : adviserId, applicantName, regionIdList, tagId, orderByField, isDesc,
						pageNum * pageSize, pageSize);
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
			if(!buildUserAdviserDto(userDto, adviserId))
				continue;
			List<ApplicantDTO> applicantList = listApplicantDto(userDo.getId(), adviserId);
			if (applicantList != null && applicantList.size() < 0)
				userDto.setApplicantList(applicantList);
			AdviserDO adviserDo = null;
			if (adviserId > 0) {
				userDto.setAdviserId(adviserId);
				adviserDo = adviserDao.getAdviserById(adviserId);
			} else if (userDto.getAdviserId() > 0)
				adviserDo = adviserDao.getAdviserById(userDto.getAdviserId());
			if (adviserDo != null)
				userDto.setAdviserDto(mapper.map(adviserDo, AdviserDTO.class));
			if (userDto.getRecommendOpenid() != null) {
				UserDTO recommendUserDto = getUserByOpenId(UserAuthTypeEnum.WECHAT.toString(),
						userDto.getRecommendOpenid());
				userDto.setRecommendUserDto(recommendUserDto);
			}
			try {
				userDto.setAuthNickname(new String(Base64Util.decodeBase64(userDto.getAuthNickname()), "utf-8"));
			} catch (Exception e) {
//				System.out.println(("昵称转码失败 userId = " + userDto.getId()));
			}
			userDto.setTagList(listTagByUserId(userDto.getId()));
			userDtoList.add(userDto);
		}
		return userDtoList;
	}

	@Override
	public UserDTO getUserById(int id) throws ServiceException {
		return getUser(id, 0);
	}

	@Override
	public UserDTO getUser(int id, int adviserId) throws ServiceException {
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
			if(!buildUserAdviserDto(userDto, adviserId))
				return userDto;
			List<ApplicantDTO> applicantList = listApplicantDto(userDo.getId(), adviserId);
			if (applicantList != null && applicantList.size() < 0)
				userDto.setApplicantList(applicantList);
			if (userDto.getAdviserId() > 0) {
				AdviserDO adviserDo = adviserDao.getAdviserById(userDto.getAdviserId());
				if (adviserDo != null)
					userDto.setAdviserDto(mapper.map(adviserDo, AdviserDTO.class));
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
//				System.out.println(("昵称转码失败 userId = " + userDto.getId()));
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
		UserDTO userDto = null;
		if (userDo != null) {
			userDto = mapper.map(userDo, UserDTO.class);
			if(!buildUserAdviserDto(userDto, userDto.getAdviserId()))
				return userDto;
			if (userDto.getAdviserId() > 0) {
				AdviserDO adviserDo = adviserDao.getAdviserById(userDto.getAdviserId());
				if (adviserDo != null)
					userDto.setAdviserDto(mapper.map(adviserDo, AdviserDTO.class));
			}
			userDto.setTagList(listTagByUserId(userDto.getId()));
		}
		return userDto;
	}

	@Override
	public boolean update(int id, String name, String authNickname, Date birthday, String phone, String email,
			String areaCode, String wechatUsername, String firstControllerContents, String visaCode,
			Date visaExpirationDate, String source) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isNotEmpty(phone)
				&& userDao.countUser(null, null, null, phone, areaCode, null, null, null, null, null) > 0) {
			List<UserDO> userList = userDao.listUser(null, null, null, phone, areaCode, null, null, null, null, null, null, null,
					null, 0, 1);
			if (userList.size() > 0 && userList.get(0).getId() != id) { // 排除当前id
				ServiceException se = new ServiceException("The phone is already existed !");
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
		}
		UserDO _userDo = userDao.getUserById(id);
		boolean isNameChange = name != null && !name.equalsIgnoreCase(_userDo.getName()); // 客户名称变化
		boolean isBirthdayChange = birthday != null && _userDo.getBirthday() != null
				&& !sdf.format(birthday).equals(sdf.format(_userDo.getBirthday())); // 客户生日变化
		boolean isVisaExpirationDate = visaExpirationDate != null && _userDo.getVisaExpirationDate() != null
				&& !sdf.format(visaExpirationDate).equals(sdf.format(_userDo.getVisaExpirationDate())); // 客户签证到期时间变化
		// 如果客户信息变化,则发送邮件给文案和mara
		if (isNameChange || isBirthdayChange || isVisaExpirationDate) {
			List<String> stateList = new ArrayList<>();
			stateList.add("PENDING");
			stateList.add("OREVIEW");
			stateList.add("REVIEW");
			stateList.add("APPLY");
			stateList.add("WAIT");
			stateList.add("PAID");
			List<ServiceOrderDO> serviceOrderList = serviceOrderDao.listServiceOrder(null, null, null, stateList, null,
					null, null, null, null, null, null, null, null, null, id, null, null, null, null, null, null, null,
					null, null, null, null, false, null, DEFAULT_PAGE_NUM, 100, null);
			for (ServiceOrderDO serviceOrderDo : serviceOrderList) {
				OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDo.getOfficialId());
				if (officialDo != null)
					sendMail(officialDo.getEmail(), "客户信息变更提醒", StringUtil.merge("亲爱的:", officialDo.getName(), "<br/>",
							"您的订单的客户信息已变更。<br>服务订单号:", serviceOrderDo.getId()));
				if ("VISA".equals(serviceOrderDo.getType())) {
					MaraDO maraDo = maraDao.getMaraById(serviceOrderDo.getMaraId());
					if (officialDo != null)
						sendMail(maraDo.getEmail(), "客户信息变更提醒", StringUtil.merge("亲爱的:", maraDo.getName(), "<br/>",
								"您的订单的客户信息已变更。<br>服务订单号:", serviceOrderDo.getId()));
				}
			}
		}
		return userDao.update(id, name, authNickname, birthday, phone, email, areaCode, wechatUsername,
				firstControllerContents, visaCode, visaExpirationDate, source);
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
		AdviserDO adviserDo = adviserDao.getAdviserById(adviserId);
		if (adviserDo == null) {
			ServiceException se = new ServiceException("adviserDto not found ! adviserId = " + adviserId);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (!AdviserStateEnum.ENABLED.toString().equals(adviserDo.getState())) {
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
			if(!buildUserAdviserDto(userDto, userDo.getAdviserId()))
				continue;
			if (userDto.getAdviserId() > 0) {
				AdviserDO adviserDo = adviserDao.getAdviserById(userDto.getAdviserId());
				if (adviserDo != null)
					userDto.setAdviserDto(mapper.map(adviserDo, AdviserDTO.class));
			}
			if (userDto.getRecommendOpenid() != null) {
				UserDTO recommendUserDto = getUserByOpenId(UserAuthTypeEnum.WECHAT.toString(),
						userDto.getRecommendOpenid());
				userDto.setRecommendUserDto(recommendUserDto);
			}
			try {
				userDto.setAuthNickname(new String(Base64Util.decodeBase64(userDto.getAuthNickname()), "utf-8"));
			} catch (Exception e) {
//				System.out.println(("昵称转码失败 userId = " + userDto.getId()));
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
	@Transactional(rollbackFor = ServiceException.class)
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

	@Override
	public int updateDOB(Date dob, int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			UserDTO userDTO = getUserById(id);
			if (userDTO.getBirthday().equals(dob)) {
				return 0;
			} else {
				return userDao.updateDOB(dob, id);
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	private boolean buildUserAdviserDto(UserDTO userDto, int adviserId) throws ServiceException {
		boolean isBelongToThisAdviser = false;
		if (ObjectUtil.isNull(userDto))
			return isBelongToThisAdviser;
		List<UserAdviserDTO> userAdviserDtoList = ListUtil.newArrayList();
		List<UserAdviserDO> userAdviserList = userDao.listUserAdviserByUserId(userDto.getId());
		if (userAdviserList != null && userAdviserList.size() > 0) {
			for (UserAdviserDO userAdviserDo : userAdviserList) {
				if (adviserId == userAdviserDo.getAdviserId()) {
					isBelongToThisAdviser = true;
					userDto.setAdviserId(userAdviserDo.getAdviserId());
				}
				UserAdviserDTO userAdviserDto = mapper.map(userAdviserDo, UserAdviserDTO.class);
				if (userAdviserDto.getAdviserId() > 0) {
					AdviserDO adviserDo = adviserDao.getAdviserById(userAdviserDto.getAdviserId());
					if (adviserDo != null)
						userAdviserDto.setAdviserDto(mapper.map(adviserDo, AdviserDTO.class));
				}
				userAdviserDtoList.add(userAdviserDto);
			}
		}
		if (isBelongToThisAdviser && userAdviserDtoList != null && userAdviserDtoList.size() > 0)
			userDto.setUserAdviserList(userAdviserDtoList);
		return isBelongToThisAdviser;
	}

	private List<ApplicantDTO> listApplicantDto(int userId, int adviserId) throws ServiceException {
		List<ApplicantDTO> applicantDtoList = new ArrayList<>();
		List<ApplicantDO> applicantList = applicantDao.list(0, null, userId, adviserId, 0, 999);
		if (applicantList != null && applicantList.size() > 0)
			for (ApplicantDO applicantDo : applicantList)
				applicantDtoList.add(mapper.map(applicantDo, ApplicantDTO.class));
		return applicantDtoList;
	}

}
