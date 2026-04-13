package org.zhinanzhen.tb.service.impl;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.request.RequestOptions;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.bitable.v1.model.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
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
	@Resource
	private ServicePackageDAO servicePackageDAO;
	@Resource
	private ServiceAssessDao serviceAssessDao;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private SimpleDateFormat sdfT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Value("${feishu.ACCESSKEYID}")
	private String ACCESS_KEY_ID;

	@Value("${feishu.ACCESSKEYSECRET}")
	private String ACCESS_KEY_SECRET;

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

		// 收集所有 userIds 和 adviserIds
		Set<Integer> adviserIds = userDoList.stream().map(UserDO::getAdviserId).collect(Collectors.toSet());
		List<Integer> adviserIdList = new ArrayList<>(adviserIds);
		List<Integer> userIds = userDoList.stream().map(UserDO::getId).collect(Collectors.toList());

		// 收集所有 recommendOpenIds
		List<String> recommendOpenIds = userDoList.stream()
				.map(UserDO::getRecommendOpenid)
				.filter(StringUtil::isNotEmpty)
				.collect(Collectors.toList());

		// ========== 批量查询 ==========

		// 批量查询 adviser
		Map<Integer, AdviserDO> adviserDOMap = new HashMap<>();
		if (!adviserIdList.isEmpty()) {
			List<AdviserDO> adviserDOS = adviserDao.listByIds(adviserIdList);
			adviserDOMap = adviserDOS.stream().collect(Collectors.toMap(AdviserDO::getId, Function.identity(), (v1, v2) -> v2));
		}

		// 批量查询 userAdviser，按 userId 分组
		Map<Integer, List<UserAdviserDO>> userAdviserMap = new HashMap<>();
		if (!userIds.isEmpty()) {
			List<UserAdviserDO> userAdviserList = userDao.listUserAdviserByUserIds(userIds);
			for (UserAdviserDO ua : userAdviserList) {
				userAdviserMap.computeIfAbsent(ua.getUserId(), k -> new ArrayList<>()).add(ua);
			}
		}

		// 批量查询 applicant，按 userId 分组
		Map<Integer, List<ApplicantDO>> applicantMap = new HashMap<>();
		if (!userIds.isEmpty()) {
			Integer queryAdviserId = adviserId > 0 ? adviserId : null;
			List<ApplicantDO> applicantList = applicantDao.listByUserIds(userIds, queryAdviserId);
			for (ApplicantDO ap : applicantList) {
				applicantMap.computeIfAbsent(ap.getUserId(), k -> new ArrayList<>()).add(ap);
			}
		}

		// 批量查询 tag，通过 user_tag 关联
		Map<Integer, List<TagDTO>> tagMap = new HashMap<>();
		if (!userIds.isEmpty()) {
			List<UserTagDO> userTagList = tagDao.listUserTagByUserIds(userIds);
			if (!userTagList.isEmpty()) {
				Set<Integer> tagIds = userTagList.stream().map(UserTagDO::getTagId).collect(Collectors.toSet());
				List<TagDO> allTags = tagDao.listTag();
				Map<Integer, TagDTO> tagByIdMap = new HashMap<>();
				for (TagDO tag : allTags) {
					if (tagIds.contains(tag.getId())) {
						tagByIdMap.put(tag.getId(), mapper.map(tag, TagDTO.class));
					}
				}
				for (UserTagDO ut : userTagList) {
					TagDTO tagDto = tagByIdMap.get(ut.getTagId());
					if (tagDto != null) {
						tagMap.computeIfAbsent(ut.getUserId(), k -> new ArrayList<>()).add(tagDto);
					}
				}
			}
		}

		// 批量查询 orderCount (VISA)
		Map<Integer, Integer> visaCountMap = new HashMap<>();
		// 批量查询 orderCount (OVST)
		Map<Integer, Integer> ovstCountMap = new HashMap<>();
		// 批量查询 orderAmount
		Map<Integer, Double> orderAmountMap = new HashMap<>();
		if (!userIds.isEmpty()) {
			List<Map<String, Object>> visaCountList = serviceOrderDao.getOrderCountBatch(userIds, "VISA");
			for (Map<String, Object> row : visaCountList) {
				visaCountMap.put(((Number) row.get("userId")).intValue(), ((Number) row.get("cnt")).intValue());
			}
			List<Map<String, Object>> ovstCountList = serviceOrderDao.getOrderCountBatch(userIds, "OVST");
			for (Map<String, Object> row : ovstCountList) {
				ovstCountMap.put(((Number) row.get("userId")).intValue(), ((Number) row.get("cnt")).intValue());
			}
			List<Map<String, Object>> amountList = serviceOrderDao.getOrderAmountBatch(userIds);
			for (Map<String, Object> row : amountList) {
				Object total = row.get("total");
				if (total != null) {
					orderAmountMap.put(((Number) row.get("userId")).intValue(), ((Number) total).doubleValue());
				}
			}
		}

		// 批量查询 cloudDiskFile，按 userId 分组
		Map<Integer, CloudDiskFile> cloudDiskFileMap = new HashMap<>();
		if (!userIds.isEmpty()) {
			List<CloudDiskFile> cloudDiskFileList = cloudDiskFileDAO.listByUserIds(userIds);
			for (CloudDiskFile cf : cloudDiskFileList) {
				if (cf.getUserId() != null) {
					cloudDiskFileMap.putIfAbsent(cf.getUserId(), cf);
				}
			}
		}

		// 批量查询 recommendUser（通过 recommend_openid 查用户）
		Map<String, UserDTO> recommendUserMap = new HashMap<>();
		if (!recommendOpenIds.isEmpty()) {
			List<UserDO> recommendUsers = userDao.listByRecommendOpenIds(recommendOpenIds);
			if (recommendUsers != null) {
				for (UserDO ru : recommendUsers) {
					UserDTO recommendDto = mapper.map(ru, UserDTO.class);
					if (ru.getAdviserId() > 0) {
						AdviserDO advDo = adviserDOMap.get(ru.getAdviserId());
						if (advDo != null)
							recommendDto.setAdviserDto(mapper.map(advDo, AdviserDTO.class));
					}
					recommendDto.setTagList(listTagByUserId(ru.getId()));
					recommendUserMap.put(ru.getRecommendOpenid(), recommendDto);
				}
			}
		}

		// ========== 循环组装数据 ==========
		for (UserDO userDo : userDoList) {
			UserDTO userDto = mapper.map(userDo, UserDTO.class);

			// cloudDiskFile
			CloudDiskFile cloudDiskFile = cloudDiskFileMap.get(userDto.getId());
			if (cloudDiskFile != null) {
				userDto.setFirstFileId(cloudDiskFile.getFileId());
			}

			// userAdviser（替代 buildUserAdviserDto 中的循环查询部分）
			boolean isBelongToThisAdviser = false;
			List<UserAdviserDTO> userAdviserDtoList = new ArrayList<>();
			List<UserAdviserDO> userAdviserDos = userAdviserMap.getOrDefault(userDto.getId(), Collections.emptyList());
			for (UserAdviserDO userAdviserDo : userAdviserDos) {
				if (adviserId == 0 || adviserId == userAdviserDo.getAdviserId()) {
					isBelongToThisAdviser = true;
					userDto.setAdviserId(userAdviserDo.getAdviserId());
				}
				UserAdviserDTO userAdviserDto = mapper.map(userAdviserDo, UserAdviserDTO.class);
				if (userAdviserDto.getAdviserId() > 0) {
					AdviserDO adviserDo = adviserDOMap.get(userAdviserDto.getAdviserId());
					if (adviserDo != null)
						userAdviserDto.setAdviserDto(mapper.map(adviserDo, AdviserDTO.class));
				}
				userAdviserDtoList.add(userAdviserDto);
			}
			if (!isBelongToThisAdviser)
				continue;
			if (!userAdviserDtoList.isEmpty())
				userDto.setUserAdviserList(userAdviserDtoList);

			// applicant
			List<ApplicantDO> applicantDos = applicantMap.getOrDefault(userDo.getId(), Collections.emptyList());
			if (!applicantDos.isEmpty()) {
				List<ApplicantDTO> applicantDtoList = new ArrayList<>();
				for (ApplicantDO ap : applicantDos) {
					applicantDtoList.add(mapper.map(ap, ApplicantDTO.class));
				}
				userDto.setApplicantList(applicantDtoList);
			}

			// adviser
			AdviserDO adviserDo = null;
			if (adviserId > 0) {
				userDto.setAdviserId(adviserId);
				adviserDo = adviserDOMap.get(adviserId);
			} else if (userDto.getAdviserId() > 0)
				adviserDo = adviserDOMap.get(userDto.getAdviserId());
			if (adviserDo != null)
				userDto.setAdviserDto(mapper.map(adviserDo, AdviserDTO.class));

			// recommendUser
			if (userDto.getRecommendOpenid() != null) {
				userDto.setRecommendUserDto(recommendUserMap.get(userDto.getRecommendOpenid()));
			}

			// authNickname 解码
			try {
				userDto.setAuthNickname(new String(Base64Util.decodeBase64(userDto.getAuthNickname()), "utf-8"));
			} catch (Exception e) {
//				System.out.println(("昵称转码失败 userId = " + userDto.getId()));
			}

			// tag
			userDto.setTagList(tagMap.getOrDefault(userDto.getId(), Collections.emptyList()));

			// orderCount & orderAmount
			userDto.setVisaCount(visaCountMap.getOrDefault(userDto.getId(), 0));
			userDto.setOvstCount(ovstCountMap.getOrDefault(userDto.getId(), 0));
			userDto.setOrderAmount(orderAmountMap.getOrDefault(userDto.getId(), 0.0));

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
	public String userData(Integer userId, String startGmtCreate, String endGmtCreate) throws Exception {
		List<Integer> officialIds = new ArrayList<>();
		officialIds.add(1000034);
		List<ServiceOrderDO> serviceOrderDOS = serviceOrderDAO.listServiceOrder(null, null, null, null, null,
				null, null, null, null, null, null,
				null, null, null, null, theDateTo00_00_00(startGmtCreate),
				theDateTo23_59_59(endGmtCreate), null, officialIds, userId, null, null, null, null, null,
				null, null, null, null, null, null, null,
				null, null, null, 0, 1000, null, null, null, null, null, null);
		if (serviceOrderDOS.isEmpty()) {
			System.out.println("没有找到服务订单数据");
			return null;
		}
		serviceOrderDOS = serviceOrderDOS.stream().sorted(Comparator.comparing(ServiceOrderDO::getFinishDate)).collect(Collectors.toList());
		List<ServiceOrderDO> visaList = new ArrayList<>();
		List<ServiceOrderDO> ovstList = new ArrayList<>();
		for (ServiceOrderDO serviceOrderDO : serviceOrderDOS) {
			int officialId = serviceOrderDO.getOfficialId();
			if (officialId == 1000044 || officialId == 1000053 || officialId == 1000056 || officialId == 1000057) {
				ovstList.add(serviceOrderDO);
			} else {
				visaList.add(serviceOrderDO);
			}
		}
		// 1. 初始化 Client
		Client client = createFeishuClient();
		// 2. 创建多维表格并获取 appToken, tableId, url
		String[] tableInfo = createBitableAndGetTableId(client);
		if (tableInfo == null) {
			System.out.println("创建多维表格失败");
			return null;
		}
		String appToken = tableInfo[0];
		String tableId = tableInfo[1];
		String url = tableInfo[2];
		// 3. 确保字段存在
		if (!ensureTableFields(client, appToken, tableId)) {
			System.out.println("确保字段存在失败");
			return null;
		}
		// 4. 构建查询数据映射
		List<OfficialDO> officialDOS = officialDao.listOfficial(null, null, null, 0, 1000);
		Map<Integer, OfficialDO> officialDOMap = officialDOS.stream().collect(Collectors.toMap(OfficialDO::getId, Function.identity(), (v1, v2) -> v2));
		List<RegionDO> regionDOS = regionDAO.listAllRegion();
		Map<Integer, RegionDO> regionDOMap = regionDOS.stream().collect(Collectors.toMap(RegionDO::getId, Function.identity(), (v1, v2) -> v2));
		List<ServiceDO> serviceDOS = serviceDAO.listService(null, null, false, 0, 1000);
		Map<Integer, ServiceDO> serviceDOMap = serviceDOS.stream().collect(Collectors.toMap(ServiceDO::getId, Function.identity(), (v1, v2) -> v2));
		List<ServicePackageDO> servicePackageListDOS = servicePackageDAO.listAll();
		Map<Integer, ServicePackageDO> servicePackageListDOMap = servicePackageListDOS.stream().collect(Collectors.toMap(ServicePackageDO::getId, Function.identity(), (v1, v2) -> v2));
		// 5. 填充数据到多维表格
		if (!populateBitableData(client, appToken, tableId, visaList, officialDOMap, regionDOMap, serviceDOMap, servicePackageListDOMap, false)) {
			System.out.println("填充数据失败");
			return null;
		}
		// 创建新的数据表
		// 创建请求对象
		CreateAppTableReq createAppTableReq = CreateAppTableReq.newBuilder()
				.appToken(appToken)
				.createAppTableReqBody(CreateAppTableReqBody.newBuilder()
						.table(ReqTable.newBuilder()
								.name("留学")
								.defaultViewName("默认的表格视图")
								.fields(new AppTableCreateHeader[] {
										AppTableCreateHeader.newBuilder()
												.fieldName("索引字段")
												.type(1)
												.build(),
										AppTableCreateHeader.newBuilder()
												.fieldName("默认")
												.type(1)
												.build(),
										AppTableCreateHeader.newBuilder()
												.fieldName("默认1")
												.type(1)
												.build(),
										AppTableCreateHeader.newBuilder()
												.fieldName("默认2")
												.type(1)
												.build()
								})
								.build())
						.build())
				.build();
		// 发起请求
		CreateAppTableResp createAppTableResp = client.bitable().v1().appTable().create(createAppTableReq);

		// 处理服务端错误
		if(!createAppTableResp.success()) {
			System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
					createAppTableResp.getCode(), createAppTableResp.getMsg(), createAppTableResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(createAppTableResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
			return null;
		}
		String tableId1 = createAppTableResp.getData().getTableId();
		if (!ensureTableFields(client, appToken, tableId1)) {
			System.out.println("确保字段存在失败");
			return null;
		}
		if (!populateBitableData(client, appToken, tableId1, ovstList, officialDOMap, regionDOMap, serviceDOMap, servicePackageListDOMap, true)) {
			System.out.println("填充数据失败");
			return null;
		}
		return url;
	}

	private Client createFeishuClient() {
		String appId = ACCESS_KEY_ID;
		String appSecret = ACCESS_KEY_SECRET;
		return Client.newBuilder(appId, appSecret).build();
	}

	private String[] createBitableAndGetTableId(Client client) throws Exception {
		// 创建多维表格
		CreateAppReq req = CreateAppReq.newBuilder()
				.reqApp(ReqApp.newBuilder()
						.name("文案订单")
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
		// 获取表格中的第一个数据表
		ListAppTableResp listTablesResp = client.bitable().appTable().list(ListAppTableReq.newBuilder()
				.appToken(appToken)
				.build());
		if (!listTablesResp.success() || listTablesResp.getData().getItems().length == 0) {
			System.out.println("获取数据表列表失败");
			return null;
		}
		String tableId = listTablesResp.getData().getItems()[0].getTableId();
		System.out.println("2. 获取到默认数据表 ID: " + tableId);
		return new String[]{appToken, tableId, url};
	}

	private boolean ensureTableFields(Client client, String appToken, String tableId) throws Exception {
		// 获取字段列表
		ListAppTableFieldReq listAppTableFieldReq = ListAppTableFieldReq.newBuilder()
				.appToken(appToken)
				.tableId(tableId)
				.pageSize(20)
				.build();
		ListAppTableFieldResp listAppTableFieldResp = client.bitable().v1().appTableField().list(listAppTableFieldReq, RequestOptions.newBuilder()
				.build());
		if (!listAppTableFieldResp.success()) {
			System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
					listAppTableFieldResp.getCode(), listAppTableFieldResp.getMsg(), listAppTableFieldResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(listAppTableFieldResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
			return false;
		}
		AppTableFieldForList[] items = listAppTableFieldResp.getData().getItems();
		int tableFieldCount = 0;
		for (AppTableFieldForList item : items) { // 更新字段信息
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
			UpdateAppTableFieldResp updateAppTableFieldResp = client.bitable().v1().appTableField().update(updateAppTableFieldReq, RequestOptions.newBuilder()
					.build());
			if (!updateAppTableFieldResp.success()) {
				System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
						updateAppTableFieldResp.getCode(), updateAppTableFieldResp.getMsg(), updateAppTableFieldResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(updateAppTableFieldResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
				return false;
			}
			tableFieldCount++;
		}
		for (int i = tableFieldCount; i < 9; i++) {
			CreateAppTableFieldReq createAppTableFieldReq = CreateAppTableFieldReq.newBuilder()
					.appToken(appToken)
					.tableId(tableId)
					.appTableField(AppTableField.newBuilder()
							.fieldName(buildTitle(tableFieldCount))
							.type(1)
							.build())
					.build();
			CreateAppTableFieldResp createAppTableFieldResp = client.bitable().v1().appTableField().create(createAppTableFieldReq, RequestOptions.newBuilder()
					.build());
			if(!createAppTableFieldResp.success()) {
				System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
						createAppTableFieldResp.getCode(), createAppTableFieldResp.getMsg(), createAppTableFieldResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(createAppTableFieldResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
				return false;
			}
			tableFieldCount++;
		}
		return true;
	}

	private AppTableRecord[] getExistingRecords(Client client, String appToken, String tableId) throws Exception {
		SearchAppTableRecordReq searchAppTableRecordReq = SearchAppTableRecordReq.newBuilder()
				.appToken(appToken)
				.tableId(tableId)
				.pageSize(20)
				.searchAppTableRecordReqBody(SearchAppTableRecordReqBody.newBuilder()
						.build())
				.build();
		SearchAppTableRecordResp searchAppTableRecordResp = client.bitable().v1().appTableRecord().search(searchAppTableRecordReq);
		if(!searchAppTableRecordResp.success()) {
			System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
					searchAppTableRecordResp.getCode(), searchAppTableRecordResp.getMsg(), searchAppTableRecordResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(searchAppTableRecordResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
			return null;
		}
		return searchAppTableRecordResp.getData().getItems();
	}

	private String buildServiceName(ServiceOrderDO serviceOrderDO,
									Map<Integer, ServiceDO> serviceDOMap,
									Map<Integer, ServicePackageDO> servicePackageListDOMap) {
		String serviceName = "";
		String ass = "";
		if (serviceOrderDO.getServiceAssessId() != null) {
			ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDO.getServiceAssessId());
			if (serviceAssessDO != null) {
				ass = serviceAssessDO.getName();
			}
		}
		if (serviceOrderDO.getServiceId() != 0) {
			ServiceDO serviceDO = serviceDOMap.get(serviceOrderDO.getServiceId());
			if (serviceOrderDO.getServicePackageId() > 0) {
				String servicepakageName = "";
				String tmp = "";
				ServicePackageDO servicePackageListDO = servicePackageListDOMap.get(serviceOrderDO.getServicePackageId());
				if (servicePackageListDO != null) {
					String servicePackagetype = servicePackageListDO.getType();
					servicepakageName = getTypeStrOfServicePackageDTO(servicePackagetype);
				}
				tmp = "-";
				if ("雇主担保".equalsIgnoreCase(serviceDO.getName()) || "独立技术移民".equalsIgnoreCase(serviceDO.getName())) {
					serviceName = serviceDO.getName() + "-" + serviceDO.getCode() + tmp + ass + servicepakageName;
				}
			} else {
				ServiceDO service = serviceDOMap.get(serviceOrderDO.getServiceId());
				if (service != null) {
					serviceName = service.getCode();
				}
			}
		} else {
			SchoolInstitutionDO schoolInstitutionDO = schoolInstitutionDAO.getSchoolInstitutionByCourseId(serviceOrderDO.getCourseId());
			if (ObjectUtil.isNotNull(schoolInstitutionDO)) {
				serviceName = schoolInstitutionDO.getName();
			}
		}
		return serviceName;
	}

	private Map<String, Object> buildRecordFields(ServiceOrderDO serviceOrderDO,
												  Map<Integer, OfficialDO> officialDOMap,
												  Map<Integer, RegionDO> regionDOMap,
												  Map<Integer, ServiceDO> serviceDOMap,
												  Map<Integer, ServicePackageDO> servicePackageListDOMap,
												  boolean convertStatus) {
		Map<String, Object> fields = new HashMap<>();
		int officialId = serviceOrderDO.getOfficialId();
		if (officialId != 0) {
			OfficialDO officialDO = officialDOMap.get(officialId);
			if (officialDO != null) {
				RegionDO regionDO = regionDOMap.get(officialDO.getRegionId());
				fields.put("所属文案", officialDO.getName());
				if (regionDO != null) {
					fields.put("所属部门", regionDO.getName());
				}
			}
		}
		String serviceName = buildServiceName(serviceOrderDO, serviceDOMap, servicePackageListDOMap);
		UserDO userById = userDao.getUserById(serviceOrderDO.getUserId());
		fields.put("完成时间", sdfT.format(serviceOrderDO.getFinishDate()));
		fields.put("客户", userById != null ? userById.getName() : "");
		fields.put("订单", String.valueOf(serviceOrderDO.getId()));
		fields.put("签证类别", serviceName);
		fields.put("AI初审结果", "这是初始写入的内容");
		fields.put("订单状态", convertStatus ? convertOrderStatus(serviceOrderDO.getState()) : serviceOrderDO.getState());
		return fields;
	}

	private boolean populateBitableData(Client client, String appToken, String tableId, List<ServiceOrderDO> serviceOrderDOS,
										Map<Integer, OfficialDO> officialDOMap, Map<Integer, RegionDO> regionDOMap,
										Map<Integer, ServiceDO> serviceDOMap, Map<Integer, ServicePackageDO> servicePackageListDOMap, boolean isInsert) throws Exception {
		AppTableRecord[] existingRecords = getExistingRecords(client, appToken, tableId);
		if (existingRecords == null) {
			return false;
		}
		if (!isInsert) {
			// 准备前10条记录的数据
			int updateCount = Math.min(serviceOrderDOS.size(), 10);
			AppTableRecord[] recordsToUpdate = new AppTableRecord[updateCount];
			for (int i = 0; i < updateCount; i++) {
				ServiceOrderDO serviceOrderDO = serviceOrderDOS.get(i);
				Map<String, Object> fields = buildRecordFields(serviceOrderDO, officialDOMap, regionDOMap, serviceDOMap, servicePackageListDOMap, true);
				recordsToUpdate[i] = AppTableRecord.newBuilder().fields(fields).build();
			}
			// 更新前10条记录
			for (int i = 0; i < updateCount; i++) {
				AppTableRecord record = existingRecords[i];
				UpdateAppTableRecordReq updateReq = UpdateAppTableRecordReq.newBuilder()
						.appToken(appToken)
						.tableId(tableId)
						.recordId(record.getRecordId())
						.appTableRecord(recordsToUpdate[i])
						.build();
				UpdateAppTableRecordResp updateResp = client.bitable().v1().appTableRecord().update(updateReq, RequestOptions.newBuilder().build());
				if (!updateResp.success()) {
					System.out.println(String.format("更新记录失败: code:%s,msg:%s,reqId:%s, resp:%s",
							updateResp.getCode(), updateResp.getMsg(), updateResp.getRequestId(),
							Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(updateResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
					return false;
				}
			}
			// 创建剩余记录
			if (serviceOrderDOS.size() > 10) {
				List<ServiceOrderDO> remainingOrders = serviceOrderDOS.subList(10, serviceOrderDOS.size());
				AppTableRecord[] recordsToCreate = new AppTableRecord[remainingOrders.size()];
				for (int i = 0; i < remainingOrders.size(); i++) {
					ServiceOrderDO serviceOrderDO = remainingOrders.get(i);
					Map<String, Object> fields = buildRecordFields(serviceOrderDO, officialDOMap, regionDOMap, serviceDOMap, servicePackageListDOMap, true);
					recordsToCreate[i] = AppTableRecord.newBuilder().fields(fields).build();
				}
				BatchCreateAppTableRecordReq createReq = BatchCreateAppTableRecordReq.newBuilder()
						.tableId(tableId)
						.appToken(appToken)
						.batchCreateAppTableRecordReqBody(BatchCreateAppTableRecordReqBody.newBuilder()
								.records(recordsToCreate)
								.build())
						.build();
				BatchCreateAppTableRecordResp createResp = client.bitable().v1().appTableRecord().batchCreate(createReq, RequestOptions.newBuilder().build());
				if (!createResp.success()) {
					System.out.println("添加记录失败: " + createResp.getMsg());
					return false;
				}
			}
		}
		if (isInsert) {
			AppTableRecord[] recordsToCreate = new AppTableRecord[serviceOrderDOS.size()];
			for (int i = 0; i < serviceOrderDOS.size(); i++) {
				ServiceOrderDO serviceOrderDO = serviceOrderDOS.get(i);
				Map<String, Object> fields = buildRecordFields(serviceOrderDO, officialDOMap, regionDOMap, serviceDOMap, servicePackageListDOMap, true);
				recordsToCreate[i] = AppTableRecord.newBuilder().fields(fields).build();
			}
			BatchCreateAppTableRecordReq createReq = BatchCreateAppTableRecordReq.newBuilder()
					.tableId(tableId)
					.appToken(appToken)
					.batchCreateAppTableRecordReqBody(BatchCreateAppTableRecordReqBody.newBuilder()
							.records(recordsToCreate)
							.build())
					.build();
			BatchCreateAppTableRecordResp createResp = client.bitable().v1().appTableRecord().batchCreate(createReq, RequestOptions.newBuilder().build());
			if (!createResp.success()) {
				System.out.println("添加记录失败: " + createResp.getMsg());
				return false;
			}
		}

		return true;
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
				title = "完成时间";
				break;
			case 1:
				title = "所属文案";
				break;
			case 2:
				title = "所属部门";
				break;
			case 3:
				title = "客户";
				break;
			case 4:
				title = "客户风险等级";
				break;
			case 5:
				title = "订单";
				break;
			case 6:
				title = "签证类别";
				break;
			case 7:
				title = "AI初审结果";
				break;
			case 8:
				title = "订单状态";
				break;
			default:
				title = "默认";
				break;
		}
		return title;
	}


	// 转换订单状态
	private String convertOrderStatus(String state) {
		String stateName = null;
		switch (state) {
			case "PENDING":
				stateName = "待提交审核";
				break;
			case "REVIEW":
				stateName = "资料待审核";
				break;
			case "OREVIEW":
				stateName = "资料审核中";
				break;
			case "APPLY":
				stateName = "服务申请中";
				break;
			case "COMPLETE":
				stateName = "服务申请完成";
				break;
			case "FINISH":
				stateName = "资料审核完成";
				break;
			case "CLOSE":
				stateName = "关闭";
				break;
			case "RECEIVED":
				stateName = "已收款凭证已提交";
				break;
			case "PAID":
				stateName = "COE已下";
				break;
			case "WAIT":
				stateName = "已提交Mara审核";
				break;
			case "APPLY_FAILED":
				stateName = "申请失败";
				break;
			case "COMPLETE_FD":
				stateName = "财务转账完成";
				break;
			default:
				stateName = "无状态";
		}
		return stateName;
	}

	private String getTypeStrOfServicePackageDTO(String type) {
		String servicepakageName;
		switch (type) {
			case "CA":
				servicepakageName = "职业评估";
				break;
			case "EOI":
				servicepakageName = "EOI";
				break;
			case "SA":
				servicepakageName = "学校申请";
				break;
			case "VA":
				servicepakageName = "签证申请";
				break;
			case "ZD":
				servicepakageName = "州担";
				break;
			case "MAT":
				servicepakageName = "Matrix";
				break;
			case "SBO":
				servicepakageName = "SBO";
				break;
			case "TM":
				servicepakageName = "提名";
				break;
			case "DB":
				servicepakageName = "担保";
				break;
			case "ROI":
				servicepakageName = "ROI";
				break;
			default:
				servicepakageName = null;
		}
		return servicepakageName;
	}
}
