package org.zhinanzhen.tb.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.utils.ImageCaptchaUtil;
import com.ikasoa.web.utils.ImageCaptchaUtil.ImageCode;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/admin_user")
public class AdminUserController extends BaseController {
	
	@Resource
	AdviserService adviserService;
	
	@RequestMapping(value = "/captcha")
	public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("image/jpeg");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expire", 0);
		ImageCode imageCode = ImageCaptchaUtil.getImageCode();
		if(imageCode != null) {
			HttpSession session = request.getSession();
			session.removeAttribute("captcha");
			session.setAttribute("captcha", imageCode.getValue());
			try {
				ImageIO.write(imageCode.getImage(), "JPEG", response.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> login(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password, @RequestParam(value = "captcha", required = false) String captcha,
			HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		super.setPostHeader(response);
		HttpSession session = request.getSession();
System.out.println("===== captcha is " + session.getAttribute("captcha") + " and " + captcha);
		int id = adminUserService.login(username, password);
		String sessionId = session.getId();
		if (id > 0 && adminUserService.updateSessionId(id, sessionId)) {
			AdminUserLoginInfo loginInfo = new AdminUserLoginInfo();
			loginInfo.setId(id);
			loginInfo.setUsername(username);
			loginInfo.setSessionId(sessionId);
			AdminUserDTO adminUser = adminUserService.getAdminUserById(id);
			if (adminUser != null) {
				String ap = adminUser.getApList();
				if (ap != null) {
					loginInfo.setApList(ap);
					if (ap.contains("GW"))
						loginInfo.setAdviserId(adminUser.getAdviserId());
					if (ap.contains("MA"))
						loginInfo.setMaraId(adminUser.getMaraId());
					if (ap.contains("WA"))
						loginInfo.setOfficialId(adminUser.getOfficialId());
					if (ap.contains("KJ"))
						loginInfo.setKjId(adminUser.getKjId());
				}
				loginInfo.setRegionId(adminUser.getRegionId());
				if (loginInfo.getAdviserId() != null && ap.contains("GW")) {
					AdviserDTO adviserDto = adviserService.getAdviserById(loginInfo.getAdviserId());
					if (adviserDto.getRegionId() == 52000 || adviserDto.getRegionId() == 1000025)
						loginInfo.setCountry("CN");
					else
						loginInfo.setCountry("AU");
				}
				
				loginInfo.setOfficialAdmin(adminUser.isOfficialAdmin());
				if (StringUtil.isNotEmpty(adminUser.getOperUserId()))
					loginInfo.setAuth(true);
			}
			session.removeAttribute("AdminUserLoginInfo" + VERSION);
			session.setAttribute("AdminUserLoginInfo" + VERSION, loginInfo);
			return new Response<Boolean>(0, true);
		} else {
			return new Response<Boolean>(0, false);
		}
	}

	@RequestMapping(value = "/out", method = RequestMethod.GET)
	@ResponseBody
	public Response<Boolean> outLogin(HttpServletRequest request, HttpServletResponse response)
			throws ServiceException {
		HttpSession session = request.getSession();
		super.setGetHeader(response);
		if (session == null)
			return new Response<Boolean>(0, true);
		AdminUserLoginInfo loginInfo = (AdminUserLoginInfo) session.getAttribute("AdminUserLoginInfo" + VERSION);
		if (loginInfo == null)
			return new Response<Boolean>(0, true);
		if (adminUserService.updateSessionId(loginInfo.getId(), null))
			session.removeAttribute("AdminUserLoginInfo" + VERSION);
		else
			return new Response<Boolean>(1, false);
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
		AdminUserLoginInfo loginInfo = (AdminUserLoginInfo) session.getAttribute("AdminUserLoginInfo" + VERSION);
		if (loginInfo == null) {
			return new Response<AdminUserLoginInfo>(1, "未登录", null);
		}
		return new Response<AdminUserLoginInfo>(0, loginInfo);
	}

	@RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updatePassword(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password, @RequestParam(value = "newPassword") String newPassword,
			HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		int id = adminUserService.login(username, password);
		if (id <= 0)
			return new Response<Boolean>(1, "用户名或密码错误", false);
		else
			return new Response<Boolean>(0, "", adminUserService.updatePassword(username, newPassword));
	}

	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> resetPassword(@RequestParam(value = "username") String username, HttpServletRequest request,
			HttpServletResponse response) throws ServiceException {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null && ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
				|| "AD".equalsIgnoreCase(adminUserLoginInfo.getApList()))) {
			String newPassword = RandomStringUtils.randomAlphanumeric(4);
			if (adminUserService.updatePassword(username, newPassword))
				return new Response<String>(0, "", newPassword);
			else
				return new Response<String>(1, "重置密码失败", null);
		}
		return new Response<String>(1, "需要超级管理员权限", null);
	}
}
