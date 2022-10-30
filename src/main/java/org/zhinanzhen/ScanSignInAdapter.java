package org.zhinanzhen;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.wechat.api.Wecom;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;

import com.ikasoa.core.utils.StringUtil;

@Component
public class ScanSignInAdapter extends BaseController implements SignInAdapter {

	@Resource
	AdviserService adviserService;

	@Override
	public String signIn(String code, Connection<?> connection, NativeWebRequest request) {
		ConnectionKey key = connection.getKey();
		if (key != null && "wecom".equalsIgnoreCase(key.getProviderId())) {
			String userId = ((Wecom) connection.getApi()).userOperations().getUserId(code);
			if (StringUtil.isEmpty(userId))
				return StringUtil.merge(Application.DOMAIN,
						"/webroot_new/user/login#Scan QR code failed! Please try again.");
			try {
				HttpSession session = request.getNativeRequest(HttpServletRequest.class).getSession();
				AdminUserDTO adminUserDto = adminUserService.getAdminUserByOpenUserId(userId);
				if (adminUserDto == null) {
					session.removeAttribute("uid");
					session.setAttribute("uid", userId);
					return StringUtil.merge(Application.DOMAIN,
							"/webroot_new/user/login#Failed! Your WeCom account has not been bound. Automatically bind after login with password.");
				}
				AdminUserLoginInfo loginInfo = getLoginInfoAndUpdateSession(session, adminUserDto.getId());
				if (loginInfo != null) {
					if (loginInfo.getAdviserId() != null) {
						AdviserDTO adviserDto = adviserService.getAdviserById(loginInfo.getAdviserId());
						if (adviserDto != null)
							loginInfo.setCountry(isCN(adviserDto.getRegionId()) ? "CN" : "AU");
					}
					return StringUtil.merge(Application.DOMAIN, "/webroot_new/welcome");
				} else
					StringUtil.merge(Application.DOMAIN,
							"/webroot_new/user/login#Login failed! Unable to get user information.");
			} catch (ServiceException e) {
				return StringUtil.merge(Application.DOMAIN, "/webroot_new/user/login#Failed: ", e.getMessage());
			}
		} else if (key != null) {
			return StringUtil.merge(Application.DOMAIN,
					"/webroot_new/user/login#Scan QR code failed! ProviderID error: ", key.getProviderId());
		}
		return StringUtil.merge(Application.DOMAIN, "/webroot_new/user/login#Scan QR code failed! Unable to get key.");
	}

}