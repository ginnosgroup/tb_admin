package org.zhinanzhen.tb.service.impl;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.request.RequestOptions;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.bitable.v1.model.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.WebLogDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.*;
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
	@Resource
	private ServiceOrderOriginallyDAO serviceOrderOriginallyDAO;
	@Resource
	private WebLogDAO webLogDAO;
	@Resource
	private CloudDiskFileDAO cloudDiskFileDAO;
	@Resource
	private ServiceOrderManageDAO serviceOrderManageDAO;
	@Resource
	private VisaDAO visaDAO;
	@Resource
	private ServiceOrderDAO serviceOrderDAO;
	@Resource
	private RegionDAO regionDAO;
	@Resource
	private ServiceDAO serviceDAO;
	@Resource
	private SchoolInstitutionDAO schoolInstitutionDAO;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public int addUser(String name, String authNickname, Date birthday, String areaCode, String phone, String email,
			String wechatUsername, String firstControllerContents, String visaCode, Date visaExpirationDate,
			String source, int adviserId, int regionId, String stateText, String channelSource) throws ServiceException {
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
//		if (phone == null) {
//			ServiceException se = new ServiceException("phone is null !");
//			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
//			throw se;
//		}
		if (adviserId <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		UserDO userDo = new UserDO();
		userDo.setName(name);
		String authNicknameUTF8 = new String(authNickname.getBytes(), StandardCharsets.UTF_8);
		userDo.setAuthNickname(authNicknameUTF8);
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
		userDo.setStateText(stateText);
		userDo.setChannelSource(channelSource);
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
				userDoList = userDao.listUser(null, name, null, authNickname, phone, areaCode, email, wechatUsername,
						adviserId <= 0 ? null : adviserId, applicantName, regionIdList, tagId, orderByField, isDesc,
						pageNum * pageSize, pageSize);
			} else {
				userDoList = userDao.listUser(null, name, authType.toString(), authNickname, phone, areaCode, email, wechatUsername,
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
			List<CloudDiskFile> cloudDiskFileList = cloudDiskFileDAO.listByParentFileId(null, "root", null, null, userDto.getId(), 0, 200);
			if (!cloudDiskFileList.isEmpty()) {
				userDto.setFirstFileId(cloudDiskFileList.get(0).getFileId());
			}
			if(!buildUserAdviserDto(userDto, adviserId))
				continue;
			List<ApplicantDTO> applicantList = listApplicantDto(userDo.getId(), adviserId);
			if (applicantList != null && applicantList.size() > 0)
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
			userDto.setVisaCount(serviceOrderDao.getOrderCount(userDto.getId(), "VISA"));
			userDto.setOvstCount(serviceOrderDao.getOrderCount(userDto.getId(), "OVST"));
			Double orderAmount = serviceOrderDao.getOrderAmount(userDto.getId());
			if(orderAmount == null) {
				orderAmount = 0.0;
				userDto.setOrderAmount(orderAmount);
			} else {
				userDto.setOrderAmount(orderAmount);
			}
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
				return null;
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
			try {
				userDto.setAuthNickname(new String(Base64Util.decodeBase64(userDto.getAuthNickname()), "utf-8"));
			} catch (Exception e) {
//				System.out.println(("昵称转码失败 userId = " + userDto.getId()));
			}
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
			Date visaExpirationDate, String source, String stateText, String channelSource, String adviserId, String apList, Integer userId) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isNotEmpty(phone)
				&& userDao.countUser(null, null, null, phone, areaCode, null, null, null, null, null) > 0) {
			List<UserDO> userList = userDao.listUser(null, null, null, null, phone, areaCode, null, null, null, null, null, null, null,
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
		boolean isVisaExpirationDateChange = visaExpirationDate != null && _userDo.getVisaExpirationDate() != null
				&& !sdf.format(visaExpirationDate).equals(sdf.format(_userDo.getVisaExpirationDate())); // 客户签证到期时间变化
		// 如果客户信息变化,则发送邮件给文案和mara
		if (isNameChange || isBirthdayChange || isVisaExpirationDateChange) {
			String changedName = "";
			String changedInfo = "";
			if (isNameChange) {
				changedName += " [姓名] ";
				changedInfo += StringUtil.merge("[姓名] 变更前: ", _userDo.getName(), " 变更后: ", name, "<br/>");
			}
			if (isBirthdayChange) {
				changedName += " [生日] ";
				changedInfo += StringUtil.merge("[生日] 变更前: ", _userDo.getBirthday(), " 变更后: ", birthday, "<br/>");
			}
			if (isVisaExpirationDateChange) {
				changedName += " [签证到期日期] ";
				changedInfo += StringUtil.merge("[签证到期日期] 变更前: ", _userDo.getVisaExpirationDate(), " 变更后: ",
						visaExpirationDate, "<br/>");
			}
			List<String> stateList = new ArrayList<>();
			stateList.add("PENDING");
			stateList.add("OREVIEW");
			stateList.add("REVIEW");
			stateList.add("APPLY");
			stateList.add("WAIT");
			stateList.add("PAID");
			List<ServiceOrderDO> serviceOrderList = serviceOrderDao.listServiceOrder(null, null,null, null, null, stateList, null,
					null, null, null, null, null, null, null, null, null, null, null, null, id, null, null, null, null, null, null, null,
					null, null, null, null,null, false, null, null, DEFAULT_PAGE_NUM, 100, null, null, null, null, null, null);
			for (ServiceOrderDO serviceOrderDo : serviceOrderList) {
				OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDo.getOfficialId());
				if (officialDo != null)
					sendMail(officialDo.getEmail(), "客户信息变更提醒", StringUtil.merge("亲爱的:", officialDo.getName(), "<br/>",
							"您的订单的客户信息", changedName, "已变更。<br>", changedInfo, "服务订单号:", serviceOrderDo.getId()));
				if ("VISA".equals(serviceOrderDo.getType())) {
					MaraDO maraDo = maraDao.getMaraById(serviceOrderDo.getMaraId());
					if (officialDo != null)
						sendMail(maraDo.getEmail(), "客户信息变更提醒", StringUtil.merge("亲爱的:", maraDo.getName(), "<br/>",
								"您的订单的客户信息", changedName, "已变更。<br>", changedInfo, "服务订单号:", serviceOrderDo.getId()));
				}
			}
		}
		if (adviserId != null && _userDo.getAdviserId() != Integer.valueOf(adviserId)) {
			WebLogDTO webLogDTO = new WebLogDTO();
			webLogDTO.setOperatedUser(_userDo.getId());
			webLogDTO.setUserId(userId);
			webLogDTO.setRole(apList);
			webLogDTO.setStartTime(sdf.format(new Date()));
			webLogDTO.setUri("/admin_v2.1/user/update");
			webLogDAO.addWebLogs(webLogDTO);

			ServiceOrderOriginallyDO serviceOrderOriginallyDO = new ServiceOrderOriginallyDO();
			serviceOrderOriginallyDO.setUserId(id);
			serviceOrderOriginallyDO.setWebLogId(webLogDTO.getId());
			serviceOrderOriginallyDO.setAdviserId(_userDo.getAdviserId());
			serviceOrderOriginallyDO.setNewAdviserId(Integer.valueOf(adviserId));
			serviceOrderOriginallyDAO.addServiceOrderOriginallyDO(serviceOrderOriginallyDO);

			userDao.updateUserAdviserById(id, adviserId);
			userDao.updateUserApplicationById(id, adviserId);
		}
		return userDao.update(id, name, authNickname, birthday, phone, email, areaCode, wechatUsername,
				firstControllerContents, visaCode, visaExpirationDate, source, stateText, channelSource, adviserId);
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

	@Override
	public UserOrder userOrder(Integer adviserId, Integer userId, int pageNum, int pageSize) {

		UserOrder userOrder = new UserOrder();
		double serviceOrderAmount = 0.0;
		double serviceOrderManageAmount = 0.0;
		int serviceOrderCount = 0;
		int serviceOrderManageCount = 0;
		List<ServiceOrderManage> serviceOrderManageList = new ArrayList<>();
		List<ServiceOrderDO> serviceOrderDOS = serviceOrderDao.listServiceOrderByUserId(userId, adviserId, false, pageNum * pageSize, pageSize);
		List<ServiceOrderDO> serviceOrderDOSManage = serviceOrderDao.listServiceOrderByUserId(userId, adviserId, true, pageNum * pageSize, pageSize);

		for (ServiceOrderDO serviceOrderDO : serviceOrderDOS) {
			if (serviceOrderDO.getApplicantParentId() == 0) {
				serviceOrderAmount += serviceOrderDO.getReceivable();
				serviceOrderCount++;
			}
		}

		for (ServiceOrderDO serviceOrderDO : serviceOrderDOSManage) {
			ServiceOrderAndManage serviceOrderAndManageById = serviceOrderManageDAO.getServiceOrderAndManageById(serviceOrderDO.getId());
			if (serviceOrderAndManageById != null) {
				ServiceOrderDO serviceOrderById = serviceOrderManageDAO.getServiceOrderById(serviceOrderAndManageById.getServiceOrderManageId());
				ServiceOrderManage manage = new ServiceOrderManage();
				BeanUtils.copyProperties(serviceOrderById, manage);
				List<ServiceOrderDTO> serviceOrderDTOS = serviceOrderManageDAO.listChildrenServiceOrder(serviceOrderById.getId());
				List<ServiceOrderDO> collect = serviceOrderDTOS.stream().map(serviceOrderDTO -> mapper.map(serviceOrderDTO, ServiceOrderDO.class)).collect(Collectors.toList());
				manage.setSubServiceOrders(collect);
				serviceOrderManageAmount += manage.getReceivable();
				List<VisaDO> visaDOS = visaDAO.listVisaByServiceOrderId(serviceOrderDO.getId());
				if (visaDOS != null && !visaDOS.isEmpty()) {
					if (visaDOS.size() == 1) {
						manage.setPaidAmount(visaDOS.get(0).getReceivable());
						manage.setUnPaidAmount(0.00);
					} else {
						for (VisaDO visaDO : visaDOS) {
							if (!visaDO.getState().equalsIgnoreCase("PENDING") || visaDO.getCommissionState().equalsIgnoreCase("YJY")) {
								manage.setPaidAmount(manage.getPaidAmount() + visaDO.getReceivable());
							} else {
								manage.setUnPaidAmount(manage.getUnPaidAmount() + visaDO.getReceivable());
							}
						}
					}
				} else {
					manage.setPaidAmount(manage.getReceivable());
					manage.setUnPaidAmount(manage.getReceivable() - manage.getAmount());
				}
				serviceOrderManageList.add(manage);
			}
		}
		serviceOrderManageCount = serviceOrderManageList.size();

		userOrder.setServiceOrderCount(serviceOrderCount + serviceOrderManageCount);
		userOrder.setServiceOrderList(serviceOrderDOS);
		userOrder.setServiceOrderAmount(serviceOrderAmount + serviceOrderManageAmount);
		userOrder.setServiceOrderManageList(serviceOrderManageList);
		return userOrder;
	}

	@Override
	public String userData(Integer userId) throws Exception {
		List<ServiceOrderDO> serviceOrderDOS = serviceOrderDAO.listServiceOrder(null, null, null, null, null,
				null, null, null, null, null, null,
				null, null, null, null, null,
				null, null, null, userId, null, null, null, null, null,
				null, null, null, null, null, null, null,
				null, null, null, 0, 20, null, null, null, null, null, null);
		// 1. 初始化 Client
		// 请替换为你的 App ID 和 App Secret
		// 获取方式：飞书开放平台 -> 应用详情 -> 凭证与基础信息
		String appId = "cli_a941d865a8399cd6";
		String appSecret = "peULZwNNWRLZzmU6MlEf1SO7WIIvUcym";
		Client client = Client.newBuilder(appId, appSecret).build();

		// 2. 创建多维表格 (Create Bitable App)
		// 接口文档: https://open.feishu.cn/document/server-docs/docs/bitable-v1/app/create
		CreateAppReq req = CreateAppReq.newBuilder()
				.reqApp(ReqApp.newBuilder()
						.name("一篇新的多维表格")
						.build())
				.build();

		CreateAppResp createAppResp = client.bitable().v1().app().create(req);
		if (!createAppResp.success()) {
			System.out.printf("创建表格失败: code: %d, msg: %s\n", createAppResp.getCode(), createAppResp.getMsg());
			return null;
		}

		String appToken = createAppResp.getData().getApp().getAppToken();
		System.out.println("1. 成功创建表格，appToken: " + appToken);

		String url = createAppResp.getData().getApp().getUrl();
		System.out.println("1. 成功创建表格，url: " + url);

		// 3. 获取表格中的第一个数据表 (Table)
		// 创建表格后，默认会生成一个数据表。我们需要它的 tableId 才能操作内容。
		ListAppTableResp listTablesResp = client.bitable().appTable().list(ListAppTableReq.newBuilder()
				.appToken(appToken)
				.build());

		if (!listTablesResp.success() || listTablesResp.getData().getItems().length == 0) {
			System.out.println("获取数据表列表失败");
			return null;
		}
		String tableId = listTablesResp.getData().getItems()[0].getTableId();
		System.out.println("2. 获取到默认数据表 ID: " + tableId);

		// 获取字段列表
		// 创建请求对象
		ListAppTableFieldReq listAppTableFieldReq = ListAppTableFieldReq.newBuilder()
				.appToken(appToken)
				.tableId(tableId)
				.pageSize(20)
				.build();

		// 发起请求
		ListAppTableFieldResp listAppTableFieldResp = client.bitable().v1().appTableField().list(listAppTableFieldReq, RequestOptions.newBuilder()
				.build());
		// 处理服务端错误
		if (!listAppTableFieldResp.success()) {
			System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
					listAppTableFieldResp.getCode(), listAppTableFieldResp.getMsg(), listAppTableFieldResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(listAppTableFieldResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
			return null;
		}

		// 业务数据处理
		System.out.println(Jsons.DEFAULT.toJson(listAppTableFieldResp.getData()));
		AppTableFieldForList[] items = listAppTableFieldResp.getData().getItems();
		int tableFieldCount = 0;
		for (AppTableFieldForList item : items) { // 更新字段信息
			// 创建请求对象
			UpdateAppTableFieldReq updateAppTableFieldReq = UpdateAppTableFieldReq.newBuilder()
					.appToken(appToken)
					.tableId(tableId)
					.fieldId(item.getFieldId())
					.appTableField(AppTableField.newBuilder()
							.fieldName(buildTitle(tableFieldCount))
							.type(1)
							.property(AppTableFieldProperty.newBuilder()
									.multiple(true)
									.build())
							.build())
					.build();

			// 发起请求
			UpdateAppTableFieldResp updateAppTableFieldResp = client.bitable().v1().appTableField().update(updateAppTableFieldReq, RequestOptions.newBuilder()
					.build());

			// 处理服务端错误
			if (!updateAppTableFieldResp.success()) {
				System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
						updateAppTableFieldResp.getCode(), updateAppTableFieldResp.getMsg(), updateAppTableFieldResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(updateAppTableFieldResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
				return null;
			}
			tableFieldCount++;
		}
		for (int i = tableFieldCount; i < 8; i++) {
			// 创建请求对象
			CreateAppTableFieldReq createAppTableFieldReq = CreateAppTableFieldReq.newBuilder()
					.appToken(appToken)
					.tableId(tableId)
					.appTableField(AppTableField.newBuilder()
							.fieldName(buildTitle(tableFieldCount))
							.type(1)
							.build())
					.build();

			// 发起请求
			CreateAppTableFieldResp createAppTableFieldResp = client.bitable().v1().appTableField().create(createAppTableFieldReq, RequestOptions.newBuilder()
					.build());

			// 处理服务端错误
			if(!createAppTableFieldResp.success()) {
				System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
						createAppTableFieldResp.getCode(), createAppTableFieldResp.getMsg(), createAppTableFieldResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(createAppTableFieldResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
				return null;
			}

			// 业务数据处理
			System.out.println(Jsons.DEFAULT.toJson(createAppTableFieldResp.getData()));
			tableFieldCount++;
		}

		// 获取记录列表
		// 创建请求对象
		SearchAppTableRecordReq searchAppTableRecordReq = SearchAppTableRecordReq.newBuilder()
				.appToken(appToken)
				.tableId(tableId)
				.pageSize(20)
				.searchAppTableRecordReqBody(SearchAppTableRecordReqBody.newBuilder()
						.build())
				.build();

		// 发起请求
		SearchAppTableRecordResp searchAppTableRecordResp = client.bitable().v1().appTableRecord().search(searchAppTableRecordReq);

		// 处理服务端错误
		if(!searchAppTableRecordResp.success()) {
			System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
					searchAppTableRecordResp.getCode(), searchAppTableRecordResp.getMsg(), searchAppTableRecordResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(searchAppTableRecordResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
			return null;
		}

		// 业务数据处理
		System.out.println(Jsons.DEFAULT.toJson(searchAppTableRecordResp.getData()));

		// 4. 添加一条初始记录 (Add Record)
		// 假设表中默认有一个多行文本列名为 "多行文本" (飞书默认模板通常包含此列)
		AppTableRecord[] appTableRecords = new AppTableRecord[serviceOrderDOS.size()];
		int count = 11;
		if (serviceOrderDOS.size() <= 10) {
			count = serviceOrderDOS.size();
		}
		List<OfficialDO> officialDOS = officialDao.listOfficial(null, null, null, 0, 1000);
		Map<Integer, OfficialDO> officialDOMap = officialDOS.stream().collect(Collectors.toMap(OfficialDO::getId, Function.identity(), (v1, v2) -> v2));
		List<RegionDO> regionDOS = regionDAO.listAllRegion();
		Map<Integer, RegionDO> regionDOMap = regionDOS.stream().collect(Collectors.toMap(RegionDO::getId, Function.identity(), (v1, v2) -> v2));
		List<ServiceDO> serviceDOS = serviceDAO.listService(null, null, false, 0, 1000);
		Map<Integer, ServiceDO> serviceDOMap = serviceDOS.stream().collect(Collectors.toMap(ServiceDO::getId, Function.identity(), (v1, v2) -> v2));
		for (int i = 0; i < count; i++) {
			ServiceOrderDO serviceOrderDO = serviceOrderDOS.get(i);
			int officialId = serviceOrderDO.getOfficialId();
			Map<String, Object> fields = new HashMap<>();
			OfficialDO officialDO = officialDOMap.get(officialId);
			RegionDO regionDO = regionDOMap.get(officialDO.getRegionId());
			String serviceName = "";
			if (serviceOrderDO.getServiceId() != 0) {
				serviceName = serviceDOMap.get(serviceOrderDO.getServiceId()).getCode();
			} else {
				SchoolInstitutionDO schoolInstitutionDO = schoolInstitutionDAO.getSchoolInstitutionByCourseId(serviceOrderDO.getCourseId());
				if (ObjectUtil.isNotNull(schoolInstitutionDO)) {
					serviceName = schoolInstitutionDO.getName();
				}
			}
			fields.put("所属文案", officialDO.getName());
			fields.put("所属部门", regionDO.getName());
			UserDO userById = userDao.getUserById(serviceOrderDO.getUserId());
			fields.put("客户", userById.getName());
			fields.put("订单", String.valueOf(serviceOrderDO.getId()));
			fields.put("签证类别", serviceName);
			fields.put("AI初审结果", "这是初始写入的内容");
			fields.put("订单状态", serviceOrderDO.getState());
			AppTableRecord build = AppTableRecord.newBuilder().fields(fields)
					.build();
			appTableRecords[i] = build;
		}

		// 记录列表数据
		AppTableRecord[] searchAppTableRecords = searchAppTableRecordResp.getData().getItems();
		for (int i = 0; i < appTableRecords.length; i++) { // 更新记录列表
			if (i < 10) {
				Map<String, Object> fields1 = appTableRecords[i].getFields();
				AppTableRecord searchAppTableRecord = searchAppTableRecords[i];
				// 创建请求对象
				UpdateAppTableRecordReq updateAppTableRecordReq = UpdateAppTableRecordReq.newBuilder()
						.appToken(appToken)
						.tableId(tableId)
						.recordId(searchAppTableRecord.getRecordId())
						.appTableRecord(AppTableRecord.newBuilder()
								.fields(fields1)
								.build())
						.build();

				// 发起请求
				UpdateAppTableRecordResp updateAppTableRecordResp = client.bitable().v1().appTableRecord().update(updateAppTableRecordReq, RequestOptions.newBuilder()
						.build());

				// 处理服务端错误
				if(!updateAppTableRecordResp.success()) {
					System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
							updateAppTableRecordResp.getCode(), updateAppTableRecordResp.getMsg(), updateAppTableRecordResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(updateAppTableRecordResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
					return null;
				}

				// 业务数据处理
				System.out.println(Jsons.DEFAULT.toJson(updateAppTableRecordResp.getData()));
			} else {
				// 创建请求对象
				BatchCreateAppTableRecordReq createRecordReq = BatchCreateAppTableRecordReq.newBuilder()
						.tableId(tableId)
						.appToken(appToken)
						.batchCreateAppTableRecordReqBody(BatchCreateAppTableRecordReqBody.newBuilder()
								.records(appTableRecords)
								.build())
						.build();

				// 发起请求
				BatchCreateAppTableRecordResp createRecordResp = client.bitable().v1().appTableRecord().batchCreate(createRecordReq, RequestOptions.newBuilder()
						.build());

				if (!createRecordResp.success()) {
					System.out.println("添加记录失败: " + createRecordResp.getMsg());
					return null;
				}
				String recordId = createRecordResp.getData().getRecords().toString();
				System.out.println("3. 成功添加记录，recordId: " + recordId);
			}
		}
		return url;
	}

	private boolean buildUserAdviserDto(UserDTO userDto, int adviserId) throws ServiceException {
		boolean isBelongToThisAdviser = false;
		if (ObjectUtil.isNull(userDto))
			return isBelongToThisAdviser;
		List<UserAdviserDTO> userAdviserDtoList = ListUtil.newArrayList();
		List<UserAdviserDO> userAdviserList = userDao.listUserAdviserByUserId(userDto.getId());
		if (userAdviserList != null && userAdviserList.size() > 0) {
			for (UserAdviserDO userAdviserDo : userAdviserList) {
				if (adviserId == 0 || adviserId == userAdviserDo.getAdviserId()) {
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

	private String buildTitle(int tableFieldCount) {
		String title = "默认";
		switch (tableFieldCount) {
			case 0:
				title = "所属文案";
				break;
			case 1:
				title = "所属部门";
				break;
			case 2:
				title = "客户";
				break;
			case 3:
				title = "客户风险等级";
				break;
			case 4:
				title = "订单";
				break;
			case 5:
				title = "签证类别";
				break;
			case 6:
				title = "AI初审结果";
				break;
			case 7:
				title = "订单状态";
				break;
			default:
				title = "默认";
				break;
		}
		return title;
	}

}
