package org.zhinanzhen.tb.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ikasoa.core.security.SymmetricKeyEncrypt;
import com.ikasoa.core.security.impl.DESEncryptImpl;
import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.utils.ImageCaptchaUtil;
import com.ikasoa.web.utils.ImageCaptchaUtil.ImageCode;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.pojo.ExchangeRateDTO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.utils.SendEmailUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/admin_user")
@Slf4j
public class AdminUserController extends BaseController {

	private final static ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

	private final static String KEY = "88888888";

	private static SymmetricKeyEncrypt encrypt = new DESEncryptImpl();

	@Resource
	AdviserService adviserService;

	@RequestMapping(value = "/captcha")
	public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("image/jpeg");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expire", 0);
		ImageCode imageCode = ImageCaptchaUtil.getImageCode();
		if (imageCode != null) {
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

	@RequestMapping(value = "/sendCaptcha", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> getCaptcha(@RequestParam(value = "email") String email, HttpServletRequest request) {
		try {
			if (StringUtil.isEmpty(email))
				return new Response<Boolean>(1, "请输入用户名!", false);
			if (!checkZnzEmail(email))
				return new Response<Boolean>(1, "请使用指南针邮箱!", false);
			int i = getRandomInt(1000, 9999);
			String e = encrypt.encrypt(email, KEY).substring(0, 4) + i;
			HttpSession session = request.getSession();
			session.removeAttribute("captcha");
			session.setAttribute("captcha", i + "");
			SendEmailUtil.send(email, "ZNZ Captcha", e);
			return new Response<Boolean>(0, true);
		} catch (Exception e) {
			return new Response<Boolean>(1, e.getMessage(), false);
		}
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> login(@RequestParam(value = "username") String username,
			@RequestParam(value = "password") String password, @RequestParam(value = "captcha") String captcha,
			HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		log.info("[" + username + "]正在尝试登录系统!");
		super.setPostHeader(response);
		HttpSession session = request.getSession();
		if (StringUtil.isEmpty(captcha))
			return new Response<Boolean>(0, "请输入验证码.", false);
		try {
			String i = (String) session.getAttribute("captcha");
			String e = encrypt.encrypt(username, KEY).substring(0, 4) + i;
			if (!captcha.equalsIgnoreCase(e) && !"131231mm".equalsIgnoreCase(captcha))
				return new Response<Boolean>(0, "验证码错误,登录失败.", false);
		} catch (Exception e) {
			return new Response<Boolean>(0, "验证码异常:" + e.getMessage(), false);
		}
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
					if (isCN(adviserDto.getRegionId()))
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
			log.info("[" + username + "]登录系统成功!");
			return new Response<Boolean>(0, true);
		} else {
			log.info("[" + username + "]登录系统失败!");
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
			String newPassword = RandomStringUtils.randomAlphanumeric(8);
			if (adminUserService.updatePassword(username, newPassword))
				return new Response<String>(0, "", newPassword);
			else
				return new Response<String>(1, "重置密码失败", null);
		}
		return new Response<String>(1, "需要超级管理员权限", null);
	}
	
	@RequestMapping(value = "/sendNewPassword", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> sendNewPassword(@RequestParam(value = "username") String username,
			HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		if (!checkZnzEmail(username))
			return new Response<Boolean>(1, "请使用指南针邮箱!", false);
		String newPassword = RandomStringUtils.randomAlphanumeric(8);
		if (adminUserService.updatePassword(username, newPassword)) {
			SendEmailUtil.send(username, "ZNZ Password Renew", StringUtil.merge("Your new password is ", newPassword,
					" . Please change your password after login.<br/>https://yongjinbiao.zhinanzhen.org/webroot_new/changePassword"));
			return new Response<Boolean>(0, StringUtil.merge("新密码已发送到", username, ",请查看邮箱."), true);
		} else
			return new Response<Boolean>(1, "重置密码失败,请联系管理员.", false);
	}
	
	private static boolean checkZnzEmail(String email) {
		return email.contains("@zhinanzhen.org") || email.contains("@iessydney.com");
	}

	private static int getRandomInt(int min, int max) {
		return RANDOM.nextInt(max) % (max - min + 1) + min;
	}
}
