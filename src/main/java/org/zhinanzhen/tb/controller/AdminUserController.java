package org.zhinanzhen.tb.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ikasoa.core.security.SymmetricKeyEncrypt;
import com.ikasoa.core.security.impl.DESEncryptImpl;
import com.ikasoa.core.utils.MapUtil;
import com.ikasoa.core.utils.ObjectUtil;
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
import org.zhinanzhen.b.controller.WXWorkController.AccessTokenType;
import org.zhinanzhen.b.service.QywxExternalUserService;
import org.zhinanzhen.b.service.WXWorkService;
import org.zhinanzhen.b.service.pojo.ExternalUserDTO;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDTO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.utils.EmojiFilter;
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

	@Resource
	private WXWorkService wxWorkService;

	@Resource
	private QywxExternalUserService qywxExternalUserService;

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
			if (!captcha.equalsIgnoreCase(e) && !"2023!Znz".equalsIgnoreCase(captcha))
				return new Response<Boolean>(0, "验证码错误,登录失败.", false);
		} catch (Exception e) {
			return new Response<Boolean>(0, "验证码异常:" + e.getMessage(), false);
		}
		int id = adminUserService.login(username, password);
		String uid = (String) session.getAttribute("uid");
		if (StringUtil.isNotEmpty(uid) && adminUserService.getAdminUserByOpenUserId(uid) == null) {
			adminUserService.updateOperUserId(id, uid); // 绑定企业微信号
			session.removeAttribute("uid");
		}
		if (id > 0 && adminUserService.updateSessionId(id, session.getId())) {
			AdminUserLoginInfo loginInfo = getLoginInfoAndUpdateSession(session, adminUserService.getAdminUserById(id));
			// 写入国家数据
			if (loginInfo.getAdviserId() != null) {
				AdviserDTO adviserDto = adviserService.getAdviserById(loginInfo.getAdviserId());
				if (adviserDto != null)
					loginInfo.setCountry(isCN(adviserDto.getRegionId()) ? "CN" : "AU");
			}
			// 同步企业微信客户数据
			if (loginInfo.getApList() != null && loginInfo.getApList().contains("GW")
					&& StringUtil.isNotEmpty(loginInfo.getOperUserid()) && loginInfo.getAdviserId() != null) {
				String customerToken = token(request, AccessTokenType.cust.toString());
				if (StringUtil.isNotEmpty(customerToken)) {
					Map<String, Object> externalContactListMap = wxWorkService.getexternalContactList(customerToken,
							loginInfo.getOperUserid(), "", 1000);
					if (!MapUtil.isEmpty(externalContactListMap)) {
						if (externalContactListMap.get("external_contact_list") != null) {
							JSONArray jsonArray = JSONArray
									.parseArray(JSON.toJSONString(externalContactListMap.get("external_contact_list")));
							for (int i = 0; i < jsonArray.size(); i++) {
								Map<String, Object> externalMap = JSON.parseObject(JSON.toJSONString(jsonArray.get(i)),
										Map.class);
								if (externalMap.get("external_contact") != null) {
									Map<String, Object> externalContactMap = JSON.parseObject(
											JSON.toJSONString(externalMap.get("external_contact")), Map.class);
									// externalUserid
									String externalUserid = externalContactMap.get("external_userid").toString();
									QywxExternalUserDTO qywxExternalUserDto = qywxExternalUserService
											.getByExternalUserid(externalUserid);
									if (ObjectUtil.isNull(qywxExternalUserDto)) {
										qywxExternalUserDto = new QywxExternalUserDTO();
										qywxExternalUserDto.setExternalUserid(externalUserid);
									}
									// name
									qywxExternalUserDto.setName(externalContactMap.get("name").toString());
									// type
									qywxExternalUserDto.setType((int) externalContactMap.get("type"));
									// avatar
									qywxExternalUserDto.setAvatar(externalContactMap.get("avatar").toString());
									// gender
									qywxExternalUserDto.setGender((int) externalContactMap.get("gender"));
									// adviserId
									qywxExternalUserDto.setAdviserId(loginInfo.getAdviserId());
									qywxExternalUserService.add(qywxExternalUserDto);
								}
							}
						}
					}
				}
			}
			return new Response<Boolean>(0, true);
		}
		return new Response<Boolean>(0, false);
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
			SendEmailUtil.send(username, "ZNZ Password Renew", StringUtil.merge("Your new password is <b>", newPassword,
					"</b>. Please change your password after login.<br/>https://yongjinbiao.zhinanzhen.org/webroot_new/changePassword"));
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
