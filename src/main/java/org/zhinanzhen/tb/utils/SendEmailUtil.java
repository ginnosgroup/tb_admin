package org.zhinanzhen.tb.utils;

import com.ikasoa.web.utils.SimpleSendEmailTool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendEmailUtil {

	private static SimpleSendEmailTool simpleSendEmailTool = new SimpleSendEmailTool("notice@zhinanzhen.org",
			"Znz@2020", SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);

	public static void send(String mail, String title, String text) {
		log.info("Send email to " + mail);
		// simpleSendEmailTool.send(mail, title, text);
	}

}
