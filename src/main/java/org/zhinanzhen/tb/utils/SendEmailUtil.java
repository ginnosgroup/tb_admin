package org.zhinanzhen.tb.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.utils.SimpleSendEmailTool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendEmailUtil {

//	private static SimpleSendEmailTool simpleSendEmailTool = new SimpleSendEmailTool("notice@zhinanzhen.org",
//			"EpibqJ2R6CFwvqiU", SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
	private static SimpleSendEmailTool txSendEmailTool = new SimpleSendEmailTool("leisu@zhinanzhen.org", "SuLei88",
			SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
	private static SimpleSendEmailTool tx1SendEmailTool = new SimpleSendEmailTool("znznotice@zhinanzhen.org", "Znz630!",
			SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
	private static SimpleSendEmailTool tx2SendEmailTool = new SimpleSendEmailTool("notice01@zhinanzhen.org", "Znz630!",
			SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
	private static SimpleSendEmailTool tx3SendEmailTool = new SimpleSendEmailTool("tasknotice@zhinanzhen.org",
			"Znz630!", SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);

//	private static SimpleSendEmailTool gmailSendEmailTool = new SimpleSendEmailTool("zhinanzhen630@gmail.com",
//			"Zhinanzhen630", SimpleSendEmailTool.SmtpServerEnum.GMAIL);
	
	private static final String WECOM_WEBHOOK = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=1afa665e-642b-4098-b4d3-4f553efe06bf";

	public static void send(String mail, String title, String content) {
//		mail = "leisu@zhinanzhen.org"; // 测试
		log.info("发送邮件:" + mail + " | " + title + "|" + content);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					tx1SendEmailTool.send(mail, title, content);
				} catch (Exception e) {
					log.error(e.getMessage());
					e.printStackTrace();
					log.info("Try send mail again!");
					try {
						tx3SendEmailTool.send(mail, title, content);
					} catch (Exception e1) {
						log.error(e1.getMessage());
						e1.printStackTrace();
						log.info("Try send mail again! (2)");
						try {
							txSendEmailTool.send(mail, title, content);
						} catch (Exception e2) {
							log.error(e2.getMessage());
							e2.printStackTrace();
						}
					}
				}
			}
		});
		thread.start();
		log.info("发送邮件完成:" + mail);
	}
	
	public static boolean sendWecomRotMsg(String content) {
		String msg = StringUtil.merge("{'msgtype': 'text', 'text': {'content': '", content, "'}}");
		try {
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5 * 1000);
			client.getHttpConnectionManager().getParams().setSoTimeout(2 * 60 * 1000);
			client.getParams().setContentCharset("UTF-8");
			PostMethod postMethod = new PostMethod(WECOM_WEBHOOK);
			postMethod.setRequestHeader("Content-Type", "applicantion/json");
			postMethod.setRequestEntity(new StringRequestEntity(msg, "applicantion/json", "UTF-8"));
			return client.executeMethod(postMethod) == HttpStatus.SC_OK;
		} catch (Exception e) {
			log.error("发送企业微信机器人信息失败:", e.getMessage());
			return false;
		}
	}

}
