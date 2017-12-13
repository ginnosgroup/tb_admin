package org.zhinanzhen.tb.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.UserDTO;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/user")
public class UserController extends BaseController {

	@Resource
	UserService userService;

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countUser(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "phone", required = false) String phone, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			int count = userService.countUser(name, authNickname, phone);
			return new Response<Integer>(0, count);
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<UserDTO>> listUser(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "authNickname", required = false) String authNickname,
			@RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "pageNum") int pageNum,
			@RequestParam(value = "pageSize") int pageSize, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			List<UserDTO> list = userService.listUser(name, authNickname, phone, pageNum, pageSize);
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

	@RequestMapping("/updateAdviser")
	@ResponseBody
	public Response<Boolean> updateAdviserId(int id, int adviserId, HttpServletResponse response)
			throws ServiceException {
		super.setGetHeader(response);
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
}
