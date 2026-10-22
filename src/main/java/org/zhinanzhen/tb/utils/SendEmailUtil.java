package org.zhinanzhen.tb.utils;

import com.ikasoa.web.utils.SimpleSendEmailTool;

import org.zhinanzhen.b.config.GlobalThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SendEmailUtil {
	private static final Logger log = LoggerFactory.getLogger(SendEmailUtil.class);
	private static final ThreadLocal<String> CURRENT_CC = new ThreadLocal<String>();
	private static final String PORTAL_UPDATE_MAIL_FROM = "znznotice_dcms@zhinanzhen.org";
	// 填入该邮箱生成的客户端专用密码，配置方式与本类现有发件账号一致。
	private static final String PORTAL_UPDATE_MAIL_PASSWORD = "sPJDVGcFKQpGiekt";
	private static final EmailService PORTAL_UPDATE_EMAIL_SERVICE = new EmailService(PORTAL_UPDATE_MAIL_FROM,
			PORTAL_UPDATE_MAIL_PASSWORD, EmailService.SmtpServerEnum.EXMAIL_QQ);

	/** 在当前请求线程中临时设置邮件抄送人；异步发送时会显式传递给邮件任务。 */
	public static CcScope withCc(String cc) {
		String previousCc = CURRENT_CC.get();
		if (cc == null || cc.trim().isEmpty())
			CURRENT_CC.remove();
		else
			CURRENT_CC.set(cc.trim());
		return new CcScope(previousCc);
	}

	public static String getCurrentCc() {
		return CURRENT_CC.get();
	}

	public static final class CcScope implements AutoCloseable {
		private final String previousCc;
		private boolean closed;

		private CcScope(String previousCc) {
			this.previousCc = previousCc;
		}

		@Override
		public void close() {
			if (closed)
				return;
			closed = true;
			if (previousCc == null)
				CURRENT_CC.remove();
			else
				CURRENT_CC.set(previousCc);
		}
	}

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

	/** 发送带抄送人的普通邮件。 */
	public static void sendWithCc(String mail, String title, String content, String cc) {
		sendWithCcInternal(mail, title, content, cc, new File[0]);
	}

	public static void sendExcel(String mail, String title, String content, File file) {
		sendAttachments(mail, title, content, file);
	}

	public static void sendAttachments(String mail, String title, String content, File... attachments) {
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
				int i = random.nextInt(simpleSendEmailToolsTmp.size());
				simpleSendEmailToolsTmp.get(i).send(mail, title, content, attachments);
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		});
		log.info("发送邮件完成: " + mail);
	}

	/** 发送带抄送人的附件邮件。 */
	public static void sendAttachmentsWithCc(String mail, String title, String content, String cc,
			File... attachments) {
		sendWithCcInternal(mail, title, content, cc, attachments);
	}

	private static void sendWithCcInternal(String mail, String title, String content, String cc, File... attachments) {
		log.info("发送邮件: " + mail + " | cc: " + cc + " | " + title + " | " + content);
		GlobalThreadPool.getInstance().execute(() -> {
			try {
				if (PORTAL_UPDATE_MAIL_PASSWORD.startsWith("REPLACE_WITH_")) {
					log.error("请在 SendEmailUtil 中配置 {} 的 SMTP 客户端专用密码，无法发送 updatePortal 邮件",
							PORTAL_UPDATE_MAIL_FROM);
					return;
				}
				PORTAL_UPDATE_EMAIL_SERVICE.sendWithCc(mail, cc, title, content, attachments);
			} catch (Exception e) {
				log.error("发送抄送邮件失败，收件人={}, 抄送人={}, 标题={}", mail, cc, title, e);
			}
		});
		log.info("发送邮件任务已提交: " + mail + " | cc: " + cc);
	}
}
