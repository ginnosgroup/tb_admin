package org.zhinanzhen.tb.utils;

import com.ikasoa.web.utils.SimpleSendEmailTool;

import org.zhinanzhen.b.config.GlobalThreadPool;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
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

	private static EmailService txSendEmailServiceTool = new EmailService("leisu@zhinanzhen.org", "SuLei88",
			EmailService.SmtpServerEnum.EXMAIL_QQ);
	private static EmailService tx1SendEmailServiceTool = new EmailService("znznotice1@zhinanzhen.org", "Znzhen6300@",
			EmailService.SmtpServerEnum.EXMAIL_QQ);
	private static EmailService tx2SendEmailServiceTool = new EmailService("znznotice2@zhinanzhen.org", "Znzhen6300@",
			EmailService.SmtpServerEnum.EXMAIL_QQ);
	private static EmailService tx3SendEmailServiceTool = new EmailService("znznotice3@zhinanzhen.org", "Znzhen6300@",
			EmailService.SmtpServerEnum.EXMAIL_QQ);
	private static EmailService tx4SendEmailServiceTool = new EmailService("znznotice4@zhinanzhen.org", "Znzhen6300@",
			EmailService.SmtpServerEnum.EXMAIL_QQ);
	private static EmailService tx5SendEmailServiceTool = new EmailService("znznotice5@zhinanzhen.org", "Znzhen6300@",
			EmailService.SmtpServerEnum.EXMAIL_QQ);

//	private static SimpleSendEmailTool tx3SendEmailTool = new SimpleSendEmailTool("tasknotice@zhinanzhen.org",
//			"Znz630!", SimpleSendEmailTool.SmtpServerEnum.EXMAIL_QQ);

//	private static SimpleSendEmailTool gmailSendEmailTool = new SimpleSendEmailTool("zhinanzhen630@gmail.com",
//			"Zhinanzhen630", SimpleSendEmailTool.SmtpServerEnum.GMAIL);

	private static List<SimpleSendEmailTool> simpleSendEmailTools = new ArrayList<SimpleSendEmailTool>();

	private static List<EmailService> emailServices = new ArrayList<EmailService>();

	public static void addSimpleSendEmailTools(SimpleSendEmailTool txSendEmailTool) {
		simpleSendEmailTools.add(txSendEmailTool);
	}

	public static void addSimpleSendEmailServiceTools(EmailService txSendEmailTool) {
		emailServices.add(txSendEmailTool);
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

	// 静态方法，用于获取静态List
	public static List<EmailService> getStaticListTmp() {
		if (emailServices != null && emailServices.size() == 0) {
			SendEmailUtil.addSimpleSendEmailServiceTools(tx1SendEmailServiceTool);
			SendEmailUtil.addSimpleSendEmailServiceTools(tx2SendEmailServiceTool);
//			SendEmailUtil.addSimpleSendEmailTools(tx3SendEmailTool);
//			SendEmailUtil.addSimpleSendEmailTools(tx4SendEmailTool);
//			SendEmailUtil.addSimpleSendEmailTools(tx5SendEmailTool);
		}
		return emailServices;
	}

	public static void send(String mail, String title, String content) {
		log.info("发送邮件: " + mail + " | " + title + "|" + content);
		GlobalThreadPool.getInstance().execute(() -> {
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
		});
		log.info("发送邮件完成: " + mail);
	}

	public static void sendExcel(String mail, String title, String content, File file) {
		log.info("发送邮件: " + mail + " | " + title + "|" + content);
		GlobalThreadPool.getInstance().execute(() -> {
			try {
				log.info(SendEmailUtil.getStaticListTmp().toString());
				List<EmailService> simpleSendEmailToolsTmp = SendEmailUtil.getStaticListTmp();
				if (simpleSendEmailToolsTmp.isEmpty()) {
					log.error("邮件服务列表为空，无法发送邮件！");
					return;
				}
				Random random = new Random();
				int i = random.nextInt(2);
				simpleSendEmailToolsTmp.get(i).send(mail, title, content, file);
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		});
		log.info("发送邮件完成: " + mail);
	}
}
