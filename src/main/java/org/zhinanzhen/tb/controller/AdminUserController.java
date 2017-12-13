package org.zhinanzhen.tb.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.tb.service.AdminUserService;
import org.zhinanzhen.tb.service.ServiceException;

import lombok.Data;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/admin_user")
public class AdminUserController extends BaseController {
    @Resource
    AdminUserService adminUserService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Response<Boolean> login(@RequestParam(value = "username") String username,
	    @RequestParam(value = "password") String password, HttpServletRequest request,
	    HttpServletResponse response) throws ServiceException {
	super.setPostHeader(response);
	HttpSession session = request.getSession();
	int id = adminUserService.login(username, password);
	String sessionId = session.getId();
	if (id > 0 && adminUserService.updateSessionId(id, sessionId)) {
	    AdminUserLoginInfo loginInfo = new AdminUserLoginInfo();
	    loginInfo.setId(id);
	    loginInfo.setUsername(username);
	    loginInfo.setSessionId(sessionId);
	    session.removeAttribute("AdminUserLoginInfo");
	    session.setAttribute("AdminUserLoginInfo", loginInfo);
	    return new Response<Boolean>(0, true);
	} else {
	    return new Response<Boolean>(0, false);
	}
    }

    @RequestMapping(value = "/out", method = RequestMethod.GET)
    @ResponseBody
    public Response<Boolean> outLogin(HttpServletRequest request, HttpServletResponse response) {
	HttpSession session = request.getSession();
	super.setGetHeader(response);
	if (session == null) {
	    return new Response<Boolean>(0, true);
	}
	session.removeAttribute("AdminUserLoginInfo");
	return new Response<Boolean>(0, true);
    }

    @RequestMapping(value = "/isLogin", method = RequestMethod.GET)
    @ResponseBody
    public Response<AdminUserLoginInfo> isLogin(HttpServletRequest request, HttpServletResponse response) {
	super.setGetHeader(response);
	HttpSession session = request.getSession();
	if (session == null) {
	    return new Response<AdminUserLoginInfo>(1, "未登录", null);
	}
	AdminUserLoginInfo loginInfo = (AdminUserLoginInfo) session.getAttribute("AdminUserLoginInfo");
	if (loginInfo == null) {
	    return new Response<AdminUserLoginInfo>(1, "未登录", null);
	}
	return new Response<AdminUserLoginInfo>(0, loginInfo);
    }

    @Data
    private class AdminUserLoginInfo {
	private int id;
	private String username;
	private String sessionId;

    }

}
