package org.zhinanzhen.tb.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.MailRemindService;
import org.zhinanzhen.b.service.pojo.MailRemindDTO;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserAuthTypeEnum;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.RegionDTO;
import org.zhinanzhen.tb.service.pojo.TagDTO;
import org.zhinanzhen.tb.service.pojo.UserAdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

import lombok.SneakyThrows;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/user")
public class UserController extends BaseController {
	
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^\\s*?(.+)@(.+?)\\s*$");

	@Resource
	UserService userService;
	
	@Resource
	RegionService regionService;

	@Resource
	MailRemindService mailRemindService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addUser(@RequestParam(value = "name") String name,
			@RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "birthday") String birthday,
			@RequestParam(value = "areaCode", required = false) String areaCode,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "firstControllerContents", required = false) String firstControllerContents,
			@RequestParam(value = "visaCode") String visaCode,
			@RequestParam(value = "visaExpirationDate", required = false) String visaExpirationDate,
			@RequestParam(value = "source") String source, @RequestParam(value = "adviserId") String adviserId,
			@RequestParam(value = "regionId", required = false) String regionId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			if (email != null && !"".equals(email) && !EMAIL_PATTERN.matcher(email).matches())
				return new Response<Integer>(1, "邮箱地址格式不正确,添加失败.", 0);
			if (phone != null && !"".equals(phone)) {
				List<UserDTO> _userList = userService.listUser(null, null, null, phone, areaCode, null, null, 0, null,
						null, null, null, null, 0, 1);
				if (_userList != null && _userList.size() > 0) {
					UserDTO _user = _userList.get(0);
					if (_user != null)
						return new Response<Integer>(1, "该电话号码已被使用,添加失败.", _user.getId());
				}
			}
			if (areaCode == null)
				areaCode = "";
			if (phone == null)
				phone = "";
			if (email == null)
				email = "";
			if (regionId == null)
				regionId = "0";
			if (name != null){
				while (name.contains("  "))
					name = name.trim().replace("  ", " "); // 处理多余空格问题
			}
			return new Response<Integer>(0,
					userService.addUser(name, authNickname, new Date(Long.parseLong(birthday.trim())), areaCode, phone,
							email, wechatUsername, firstControllerContents, visaCode,
							new Date(Long.parseLong(visaExpirationDate)), source, StringUtil.toInt(adviserId),
							StringUtil.toInt(regionId)));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/addUserAdviser", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addUserAdviser(@RequestParam(value = "userId") String userId,
			@RequestParam(value = "adviserId") String adviserId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			if (ObjectUtil.isNull(getAdminUserLoginInfo(request)))
				return new Response<Integer>(1, "No permission !", null);
			return new Response<Integer>(0,
					userService.addUserAdviser(StringUtil.toInt(userId), StringUtil.toInt(adviserId)));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Response<Integer> countUser(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "authType", required = false) String authType,
			@RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "phone", required = false) String areaCode,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "applicantName", required = false) String applicantName,
			@RequestParam(value = "regionId", required = false) Integer regionId, HttpServletRequest request,
			@RequestParam(value = "tagId", required = false) String tagId, HttpServletResponse response) {
		
		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		try {
			super.setGetHeader(response);
			UserAuthTypeEnum authTypeEnum = null;
			if (StringUtil.isNotEmpty(authType)) {
				authTypeEnum = UserAuthTypeEnum.get(authType);
			}
			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (ObjectUtil.isNull(adminUserLoginInfo))
				return new Response<Integer>(1, "No permission !", null);
			if ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				if (regionIdList == null) {
					List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
					regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
					for (RegionDTO region : regionList)
						regionIdList.add(region.getId());
				}
			} else {
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId + "";
				if (StringUtil.isBlank(adviserId) && !isAdminUser(request))
					return new Response<Integer>(1, "No permission !", -1);
			}
			int count = userService.countUser(name, authTypeEnum, authNickname, phone, areaCode, wechatUsername,
					StringUtil.toInt(adviserId), applicantName, regionIdList, StringUtil.toInt(tagId));
			return new Response<Integer>(0, count);
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/countMonth", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countUserByThisMonth(HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, userService.countUserByThisMonth(getAdviserId(request), null));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<UserDTO>> listUser(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "authType", required = false) String authType,
			@RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "areaCode", required = false) String areaCode,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "applicantName", required = false) String applicantName,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "orderByField", required = false) String orderByField,
			@RequestParam(value = "isDesc", required = false) String isDesc,
			@RequestParam(value = "tagId", required = false) String tagId, @RequestParam(value = "pageNum") int pageNum,
			@RequestParam(value = "pageSize") int pageSize, HttpServletRequest request,
			HttpServletResponse response) {

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		try {
			super.setGetHeader(response);
			UserAuthTypeEnum authTypeEnum = null;
			if (StringUtil.isNotEmpty(authType)) {
				authTypeEnum = UserAuthTypeEnum.get(authType);
			}
			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (ObjectUtil.isNull(adminUserLoginInfo))
				return new ListResponse<List<UserDTO>>(false, pageSize, 0, null, "No permission !");
			if ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList()) && adminUserLoginInfo.getRegionId() != null
					&& adminUserLoginInfo.getRegionId() > 0) {
				if (regionIdList == null) {
					List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
					regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
					for (RegionDTO region : regionList)
						regionIdList.add(region.getId());
				}
			} else {
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId + "";
				if (StringUtil.isBlank(adviserId) && !isAdminUser(request))
					return new ListResponse<List<UserDTO>>(false, pageSize, 0, null, "No permission !");
			}

			if (id != null && id > 0){
				List<UserDTO> list = new ArrayList<>();
				UserDTO userDTO = userService.getUser(id, StringUtil.toInt(adviserId));
				userDTO.setMailRemindDTOS(
						mailRemindService.list(getAdviserId(request), null, null, null, null, null, id, false, false));
				list.add(userDTO);
				return new ListResponse<List<UserDTO>>(true, pageSize, 1, list, "");
			}

			int total = userService.countUser(name, authTypeEnum, authNickname, phone, areaCode, wechatUsername,
					StringUtil.toInt(adviserId), applicantName, regionIdList, StringUtil.toInt(tagId));
			List<UserDTO> list = userService.listUser(name, authTypeEnum, authNickname, phone, areaCode, email,
					wechatUsername, StringUtil.toInt(adviserId), applicantName, regionIdList, StringUtil.toInt(tagId),
					orderByField, Boolean.parseBoolean(StringUtil.isEmpty(isDesc) ? "false" : isDesc), pageNum,
					pageSize);
			for (UserDTO user : list) {
				List<MailRemindDTO> mailRemindDTOS = mailRemindService.list(getAdviserId(request),null,null,null,null,null,user.getId(),false,true);
				user.setMailRemindDTOS(mailRemindDTOS);
			}
			return new ListResponse<List<UserDTO>>(true, pageSize, total, list, "");
		} catch (ServiceException e) {
			return new ListResponse<List<UserDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	@SneakyThrows
	public Response<UserDTO> getUser(@RequestParam(value = "id") int id, HttpServletRequest request,
			HttpServletResponse response) {
		super.setGetHeader(response);
		Integer adviserId = getAdviserId(request);
		if (ObjectUtil.isNull(adviserId) && !isAdminUser(request) && !isSuperAdminUser(request))
			return new Response<UserDTO>(1, "No permission !", null);
		UserDTO user = userService.getUser(id, adviserId);
		return new Response<UserDTO>(0, user);
	}

	@RequestMapping(value = "/getByPhone", method = RequestMethod.GET)
	@ResponseBody
	public Response<UserDTO> getByPhone(@RequestParam(value = "phone") String phone,
			@RequestParam(value = "areaCode", required = false) String areaCode, HttpServletRequest request,
			HttpServletResponse response) throws ServiceException {
		if (phone == null) {
			return new Response<UserDTO>(1, "参数错误.");
		}
		Integer adviserId = getAdviserId(request);
		if (ObjectUtil.isNull(adviserId) && !isAdminUser(request) && !isSuperAdminUser(request))
			return new Response<UserDTO>(1, "No permission !", null);
		super.setGetHeader(response);
		List<UserDTO> list = userService.listUser(null, null, null, phone, areaCode, null, null, 0, null, null, 0, 1);
		if (list != null && list.size() > 0) {
			UserDTO user = list.get(0);
			List<UserAdviserDTO> userAdviserList = user.getUserAdviserList();
			if (userAdviserList != null && userAdviserList.size() > 0) {
				for (UserAdviserDTO userAdviserDto : userAdviserList)
					if (userAdviserDto.getAdviserId() > 0 && adviserId != null
							&& userAdviserDto.getAdviserId() == adviserId)
						return new Response<UserDTO>(0, "该手机号所属客户属于该顾问.", user);
			}
			return new Response<UserDTO>(0, user);
		} else
			return new Response<UserDTO>(0, null, null);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "birthday", required = false) String birthday,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "areaCode", required = false) String areaCode,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "firstControllerContents", required = false) String firstControllerContents,
			@RequestParam(value = "visaCode", required = false) String visaCode,
			@RequestParam(value = "visaExpirationDate", required = false) String visaExpirationDate,
			@RequestParam(value = "source", required = false) String source, HttpServletRequest request,
			HttpServletResponse response) throws ServiceException {
		super.setPostHeader(response);
		if (ObjectUtil.isNull(getAdminUserLoginInfo(request)))
			return new Response<Boolean>(1, "No permission !", false);
		Date _birthday = null;
		if (birthday != null)
			_birthday = new Date(Long.parseLong(birthday.trim()));
		Date _visaExpirationDate = null;
		if (visaExpirationDate != null)
			_visaExpirationDate = new Date(Long.parseLong(visaExpirationDate.trim()));
		if (name != null)
			name = name.trim().replace("  ", " "); // 处理多余空格问题
		return new Response<Boolean>(0, userService.update(id, name, authNickname, _birthday, phone, email, areaCode,
				wechatUsername, firstControllerContents, visaCode, _visaExpirationDate, source));
	}

	@RequestMapping(value = "/updateAdviser", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateAdviserId(@RequestParam(value = "id") int id,
			@RequestParam(value = "adviserId") int adviserId, HttpServletRequest request, HttpServletResponse response)
			throws ServiceException {
		super.setPostHeader(response);
		if (id <= 0 || adviserId <= 0)
			return new Response<Boolean>(1, "请检查参数!");
		// TODO: 这里或许要判断一下顾问管理员
		if (ObjectUtil.isNull(getAdminUserLoginInfo(request)))
			return new Response<Boolean>(1, "No permission !", false);
		return new Response<Boolean>(0, userService.updateAdviserId(adviserId, id));
	}

	@RequestMapping(value = "/listByRecommendUserId", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<UserDTO>> ListByRecommendOpenId(int userId, HttpServletRequest request)
			throws ServiceException {
		if (userId <= 0) {
			return new Response<List<UserDTO>>(2, "userId错误");
		}
		if (ObjectUtil.isNull(getAdminUserLoginInfo(request)))
			return new Response<List<UserDTO>>(1, "No permission !", null);
		UserDTO userDto = userService.getUserById(userId);
		if (userDto == null) {
			return new Response<List<UserDTO>>(2, "顾客未找到, userId = " + userId);
		}
		String AuthOpenId = userDto.getAuthOpenid();
		List<UserDTO> list = userService.listUserByRecommendOpenId(AuthOpenId);
		return new Response<List<UserDTO>>(0, list);
	}

	@RequestMapping(value = "/listByRecommendUserIdCount", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> ListByRecommendOpenIdCount(int userId, HttpServletRequest request)
			throws ServiceException {
		if (userId <= 0) {
			return new Response<Integer>(2, "userId错误");
		}
		if (ObjectUtil.isNull(getAdminUserLoginInfo(request)))
			return new Response<Integer>(1, "No permission !", null);
		UserDTO userDto = userService.getUserById(userId);
		if (userDto == null) {
			return new Response<Integer>(2, "顾客未找到, userId = " + userId);
		}
		String AuthOpenId = userDto.getAuthOpenid();
		List<UserDTO> list = userService.listUserByRecommendOpenId(AuthOpenId);
		return new Response<Integer>(0, list.size());
	}

	@RequestMapping(value = "/newTag", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> newTag(@RequestParam(value = "name") String name, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, userService.newTag(name));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/addTag", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addTag(@RequestParam(value = "userId") String userId,
			@RequestParam(value = "tagId") String tagId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			Integer _userId = Integer.parseInt(userId);
			if (userService.getUserById(_userId) == null)
				return new Response<Integer>(1, "用户不存在!", -1);
			Integer _tagId = Integer.parseInt(tagId);
			if (userService.getTag(_tagId) == null)
				return new Response<Integer>(1, "TAG不存在!", -1);
			for (TagDTO tag : userService.listTagByUserId(_userId))
				if (tag.getId() == _tagId)
					return new Response<Integer>(1, "该TAG已添加过!", -1);
			return new Response<Integer>(0, userService.addTag(_userId, _tagId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/listTag", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<TagDTO>> listTag() throws ServiceException {
		return new Response<List<TagDTO>>(0, userService.listTag());
	}

	@RequestMapping(value = "/listTagByUserId", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<TagDTO>> listTagByUserId(@RequestParam(value = "userId") String userId)
			throws ServiceException {
		return new Response<List<TagDTO>>(0, userService.listTagByUserId(Integer.parseInt(userId)));
	}

	@RequestMapping(value = "/deleteTagById", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteTagById(@RequestParam(value = "id") String id) throws ServiceException {
		return new Response<Integer>(0, userService.deleteTagById(Integer.parseInt(id)));
	}

	@RequestMapping(value = "/deleteUserTagByUserId", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteUserTagByUserId(@RequestParam(value = "userId") String userId)
			throws ServiceException {
		return new Response<Integer>(0, userService.deleteUserTagByUserId(Integer.parseInt(userId)));
	}

	@RequestMapping(value = "/deleteUserTagByTagIdAndUserId", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteUserTagByTagIdAndUserId(@RequestParam(value = "tagId") String tagId,
			@RequestParam(value = "userId") String userId) throws ServiceException {
		return new Response<Integer>(0,
				userService.deleteUserTagByTagIdAndUserId(Integer.parseInt(tagId), Integer.parseInt(userId)));
	}
	
	@RequestMapping(value = "/untrueList", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<UserDTO>> untrueList(@RequestParam(value = "authType", required = false) String authType,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "regionId", required = false) Integer regionId,HttpServletRequest request, HttpServletResponse response) {
		
		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		try {
			super.setGetHeader(response);
			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				if (regionIdList == null) {
					List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
					regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
					for (RegionDTO region : regionList)
						regionIdList.add(region.getId());
				}
			} else {
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId + "";
				if (StringUtil.isBlank(adviserId) && !isAdminUser(request))
					return new Response<List<UserDTO>>(1, "No permission !", null);
			}
			List<UserDTO> list = userService.listUser(null, null, null, null, null, null, null, StringUtil.toInt(adviserId), null,
					regionIdList, null, null, false, 0, 9999);
			List<UserDTO> _list = new ArrayList<UserDTO>();
			for (UserDTO user : list) {
				String phone = user.getPhone();
				String areaCode = user.getAreaCode();
				if (!isNumber(phone) || StringUtil.isEmpty(areaCode) || "+86".equals(areaCode) && phone.length() != 11
						|| "+61".equals(areaCode) && phone.length() != 10)
					_list.add(user);
			}
			return new Response<List<UserDTO>>(0, _list);
		} catch (ServiceException e) {
			return new Response<List<UserDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/untrueCount", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> untrueCount(@RequestParam(value = "authType", required = false) String authType,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "regionId", required = false) Integer regionId, HttpServletRequest request,
			HttpServletResponse response) {

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		try {
			super.setGetHeader(response);
			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				if (regionIdList == null) {
					List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
					regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
					for (RegionDTO region : regionList)
						regionIdList.add(region.getId());
				}
			} else {
				Integer newAdviserId = getAdviserId(request);
				if (newAdviserId != null)
					adviserId = newAdviserId + "";
				if (StringUtil.isBlank(adviserId) && !isAdminUser(request))
					return new Response<Integer>(1, "No permission !", null);
			}
			List<UserDTO> list = userService.listUser(null, null, null, null, null, null, null, StringUtil.toInt(adviserId), null,
					regionIdList, null, null, false, 0, 9999);
			Integer count = 0;
			for (UserDTO user : list) {
				String phone = user.getPhone();
				String areaCode = user.getAreaCode();
				if (!isNumber(phone) || StringUtil.isEmpty(areaCode) || "+86".equals(areaCode) && phone.length() != 11
						|| "+61".equals(areaCode) && phone.length() != 10)
					count++;
			}
			return new Response<Integer>(0, count);
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	private static boolean isNumber(String string) {
		if (StringUtil.isEmpty(string))
			return false;
		Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d+)?$");
		return pattern.matcher(string).matches();
	}

}
