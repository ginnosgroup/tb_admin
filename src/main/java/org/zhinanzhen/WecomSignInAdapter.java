package org.zhinanzhen;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.wechat.api.User;
import org.springframework.social.wechat.api.Wecom;
import org.springframework.web.context.request.NativeWebRequest;

public class WecomSignInAdapter implements SignInAdapter {

	@Override
	public String signIn(String openId, Connection<?> connection, NativeWebRequest request) {
		ConnectionKey key = connection.getKey();
		if ("wecom".equalsIgnoreCase(key.getProviderId())) {
			User user = ((Wecom) connection.getApi()).userOperations().getUserProfile(openId);
			
			System.out.println(user); // 打印微信用户详细信息
		}
		return "/webroot_new/welcome";
	}

}
