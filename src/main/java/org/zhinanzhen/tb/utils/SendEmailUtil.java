package org.zhinanzhen.tb.utils;

import com.ikasoa.web.utils.SimpleSendEmailTool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendEmailUtil {

	private static SimpleSendEmailTool simpleSendEmailTool = new SimpleSendEmailTool("notice@zhinanzhen.org",
			"Znz@2020", SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);

	public static void send(String mail, String title, String text) {
		mail = "7311930@qq.com"; // 测试
		try {
			simpleSendEmailTool.send(mail, title, text);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}
