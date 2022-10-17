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
				return StringUtil.merge(Application.DOMAIN, "/webroot_new/user/login#扫码失败!请重试.");
			try {
				HttpSession session = request.getNativeRequest(HttpServletRequest.class).getSession();
				AdminUserDTO adminUserDto = adminUserService.getAdminUserByOpenUserId(userId);
				if (adminUserDto == null) {
					session.removeAttribute("uid");
					session.setAttribute("uid", userId);
					return StringUtil.merge(Application.DOMAIN,
							"/webroot_new/user/login#扫码登录失败!您还未绑定企业微信号,请使用账户密码登录后自动绑定.");
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
					StringUtil.merge(Application.DOMAIN, "/webroot_new/user/login#登录失败!请联系管理员.");
			} catch (ServiceException e) {
				return StringUtil.merge(Application.DOMAIN, "/webroot_new/user/login#登录失败:", e.getMessage());
			}
		} else if (key != null) {
			return StringUtil.merge(Application.DOMAIN, "/webroot_new/user/login#扫码失败!ProviderID错误:",
					key.getProviderId());
		}
		return StringUtil.merge(Application.DOMAIN, "/webroot_new/user/login#扫码失败!无法获取Key.");
	}

}
