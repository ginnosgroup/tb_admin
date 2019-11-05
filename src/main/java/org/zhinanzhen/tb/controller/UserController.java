package org.zhinanzhen.tb.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserAuthTypeEnum;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.TagDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/user")
public class UserController extends BaseController {

	@Resource
	UserService userService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addUser(@RequestParam(value = "name") String name,
			@RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "birthday") String birthday,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "firstControllerContents", required = false) String firstControllerContents,
			@RequestParam(value = "visaCode") String visaCode,
			@RequestParam(value = "visaExpirationDate", required = false) String visaExpirationDate,
			@RequestParam(value = "source") String source, @RequestParam(value = "adviserId") String adviserId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			if (phone != null && !"".equals(phone) && userService.countUser(null, null, null, phone, null, 0) > 0)
				return new Response<Integer>(1, "该电话号码已被使用,添加失败.", 0);
			if (phone == null)
				phone = "";
			return new Response<Integer>(0,
					userService.addUser(name, authNickname, new Date(Long.parseLong(birthday.trim())), phone,
							wechatUsername, firstControllerContents, visaCode,
							new Date(Long.parseLong(visaExpirationDate)), source, StringUtil.toInt(adviserId)));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countUser(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "authType", required = false) String authType,
			@RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "adviserId", required = false) String adviserId, HttpServletRequest request,
			HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";
		if (StringUtil.isBlank(adviserId) && !isAdminUser(request))
			return new Response<Integer>(1, "No permission !", -1);

		try {
			super.setGetHeader(response);
			UserAuthTypeEnum authTypeEnum = null;
			if (StringUtil.isNotEmpty(authType)) {
				authTypeEnum = UserAuthTypeEnum.get(authType);
			}
			int count = userService.countUser(name, authTypeEnum, authNickname, phone, wechatUsername,
					StringUtil.toInt(adviserId));
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
			return new Response<Integer>(0, userService.countUserByThisMonth(getAdviserId(request)));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<UserDTO>> listUser(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "authType", required = false) String authType,
			@RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "orderByField", required = false) String orderByField,
			@RequestParam(value = "isDesc", required = false) String isDesc,
			@RequestParam(value = "tagId", required = false) String tagId, @RequestParam(value = "pageNum") int pageNum,
			@RequestParam(value = "pageSize") int pageSize, HttpServletRequest request, HttpServletResponse response) {

		// 更改当前顾问编号
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId + "";
		if (StringUtil.isBlank(adviserId) && !isAdminUser(request))
			return new Response<List<UserDTO>>(1, "No permission !", null);

		try {
			super.setGetHeader(response);
			UserAuthTypeEnum authTypeEnum = null;
			if (StringUtil.isNotEmpty(authType)) {
				authTypeEnum = UserAuthTypeEnum.get(authType);
			}
			List<UserDTO> list = userService.listUser(name, authTypeEnum, authNickname, phone, wechatUsername,
					StringUtil.toInt(adviserId), StringUtil.toInt(tagId), orderByField,
					Boolean.parseBoolean(StringUtil.isEmpty(isDesc) ? "false" : isDesc), pageNum, pageSize);
			return new Response<List<UserDTO>>(0, list);
		} catch (ServiceException e) {
			return new Response<List<UserDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<UserDTO> getUser(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			UserDTO user = userService.getUserById(id);
			return new Response<UserDTO>(0, user);
		} catch (ServiceException e) {
			return new Response<UserDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "birthday", required = false) String birthday,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "firstControllerContents", required = false) String firstControllerContents,
			@RequestParam(value = "visaCode", required = false) String visaCode,
			@RequestParam(value = "visaExpirationDate", required = false) String visaExpirationDate,
			@RequestParam(value = "source", required = false) String source, HttpServletResponse response)
			throws ServiceException {
		super.setPostHeader(response);
		Date _birthday = null;
		if (birthday != null)
			_birthday = new Date(Long.parseLong(birthday.trim()));
		Date _visaExpirationDate = null;
		if (visaExpirationDate != null)
			_visaExpirationDate = new Date(Long.parseLong(visaExpirationDate.trim()));
		return new Response<Boolean>(0, userService.update(id, name, authNickname, _birthday, phone, wechatUsername,
				firstControllerContents, visaCode, _visaExpirationDate, source));
	}

	@RequestMapping(value = "/updateAdviser", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateAdviserId(int id, int adviserId, HttpServletResponse response)
			throws ServiceException {
		super.setPostHeader(response);
		return new Response<Boolean>(0, userService.updateAdviserId(adviserId, id));
	}

	@RequestMapping(value = "/listByRecommendUserId", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<UserDTO>> ListByRecommendOpenId(int userId) throws ServiceException {
		if (userId <= 0) {
			return new Response<List<UserDTO>>(2, "userId错误");
		}
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
	public Response<Integer> ListByRecommendOpenIdCount(int userId) throws ServiceException {
		if (userId <= 0) {
			return new Response<Integer>(2, "userId错误");
		}
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

}
