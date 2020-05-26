package org.zhinanzhen.tb.utils;

public class SendEmailUtil {

	private static SimpleSendEmailTool simpleSendEmailTool = new SimpleSendEmailTool("notice@zhinanzhen.org",
			"Znz@2020", SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);

	public static void send(String mail, String title, String text) {
		mail = "7311930@qq.com"; // 测试
System.out.println("+++++ mail:" + mail + ", title:" + title + ", text:" + text);
		simpleSendEmailTool.send(mail, title, text);
	}

}
