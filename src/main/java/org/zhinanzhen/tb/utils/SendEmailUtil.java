package org.zhinanzhen.tb.utils;

import com.ikasoa.web.utils.SimpleSendEmailTool;

import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class SendEmailUtil {

//	private static SimpleSendEmailTool simpleSendEmailTool = new SimpleSendEmailTool("notice@zhinanzhen.org",
//			"EpibqJ2R6CFwvqiU", SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
	private static SimpleSendEmailTool txSendEmailTool = new SimpleSendEmailTool("leisu@zhinanzhen.org", "SuLei88",
			SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
	private static SimpleSendEmailTool tx1SendEmailTool = new SimpleSendEmailTool("znznotice1@zhinanzhen.org", "Znzhen6300@",
			SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
	private static SimpleSendEmailTool tx2SendEmailTool = new SimpleSendEmailTool("znznotice2@zhinanzhen.org", "Znzhen6300@",
			SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
	private static SimpleSendEmailTool tx3SendEmailTool = new SimpleSendEmailTool("znznotice3@zhinanzhen.org", "Znzhen6300@",
			SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
	private static SimpleSendEmailTool tx4SendEmailTool = new SimpleSendEmailTool("znznotice4@zhinanzhen.org", "Znzhen6300@",
			SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
	private static SimpleSendEmailTool tx5SendEmailTool = new SimpleSendEmailTool("znznotice5@zhinanzhen.org", "Znzhen6300@",
			SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);
//	private static SimpleSendEmailTool tx3SendEmailTool = new SimpleSendEmailTool("tasknotice@zhinanzhen.org",
//			"Znz630!", SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);

//	private static SimpleSendEmailTool gmailSendEmailTool = new SimpleSendEmailTool("zhinanzhen630@gmail.com",
//			"Zhinanzhen630", SimpleSendEmailTool.SmtpServerEnum.GMAIL);

	private static List<SimpleSendEmailTool> simpleSendEmailTools = new ArrayList<SimpleSendEmailTool>();

	public static void addSimpleSendEmailTools(SimpleSendEmailTool txSendEmailTool) {
		simpleSendEmailTools.add(txSendEmailTool);
	}

	// 静态方法，用于获取静态List
	public static List<SimpleSendEmailTool> getStaticList() {
		if (simpleSendEmailTools != null && simpleSendEmailTools.size() == 0) {
			SendEmailUtil.addSimpleSendEmailTools(tx1SendEmailTool);
			SendEmailUtil.addSimpleSendEmailTools(tx2SendEmailTool);
//			SendEmailUtil.addSimpleSendEmailTools(tx3SendEmailTool);
//			SendEmailUtil.addSimpleSendEmailTools(tx4SendEmailTool);
//			SendEmailUtil.addSimpleSendEmailTools(tx5SendEmailTool);
		}
		return simpleSendEmailTools;
	}

	public static void send(String mail, String title, String content) {
		log.info("发送邮件:" + mail + " | " + title + "|" + content);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					log.info(SendEmailUtil.getStaticList().toString());
					List<SimpleSendEmailTool> simpleSendEmailToolsTmp = SendEmailUtil.getStaticList();
					Random random = new Random();
					int i = random.nextInt(2);
					simpleSendEmailToolsTmp.get(i).send(mail, title, content);
				} catch (Exception e) {
					log.error(e.getMessage());
					e.printStackTrace();
				}
			}
		});
		thread.start();
		log.info("发送邮件完成:" + mail);
	}

}
