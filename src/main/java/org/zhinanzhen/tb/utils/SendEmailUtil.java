package org.zhinanzhen.tb.utils;

import com.ikasoa.web.utils.SimpleSendEmailTool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendEmailUtil {

	private static SimpleSendEmailTool simpleSendEmailTool = new SimpleSendEmailTool("notice@zhinanzhen.org",
			"EpibqJ2R6CFwvqiU", SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);

	public static void send(String mail, String title, String text) {
//		mail = "7311930@qq.com"; // 测试
		try {
			log.info("发送邮件:" + mail + " | " + title + "|" + text);
			simpleSendEmailTool.send(mail, title, text);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}
