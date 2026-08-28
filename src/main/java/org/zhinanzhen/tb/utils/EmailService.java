package org.zhinanzhen.tb.utils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Properties;

public class EmailService {
    private String from = null;
    private String password = null;
    private SmtpServerEnum smtpServer = null;

	public EmailService() {
	}

	public EmailService(String from, String password, SmtpServerEnum smtpServer) {
		this.from = from;
		this.password = password;
		this.smtpServer = smtpServer;
	}

    // 发送带附件邮件的方法（重载）
	public void send(String mail, String title, String text, File attachment) throws Exception {
		this.send(mail, title, text, attachment == null ? new File[0] : new File[] { attachment });
	}

	// 发送带多个附件的邮件
	public void send(String mail, String title, String text, File... attachments) throws Exception {
        Properties prop = new Properties();
        prop.setProperty("mail.host", smtpServer.getHost());
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");
        prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.setProperty("mail.smtp.socketFactory.port", smtpServer.getPort());
        prop.setProperty("mail.smtp.port", smtpServer.getPort());

        Session session = Session.getInstance(prop);
        session.setDebug(true);
        Transport ts = session.getTransport();
        ts.connect(smtpServer.getHost(), from, password);

        // 调用新的方法创建带附件的邮件
        Message message = createAttachmentMail(session, from, mail, title, text, attachments);
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();
    }

    // 原方法保持不变（发送纯文本）
	public void send(String mail, String title, String text) throws Exception {
		this.send(mail, title, text, new File[0]);
    }

    // 创建带附件的邮件内容
    private MimeMessage createAttachmentMail(Session session, String mailFrom, String mailTo, String title,
												String text, File... attachments) throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailFrom));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
		message.setSubject(title, "UTF-8");

        // 创建 Multipart 容器（文本 + 附件）
        Multipart multipart = new MimeMultipart();

        // 1. 添加文本内容
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(text, "text/html;charset=UTF-8");
        multipart.addBodyPart(textPart);

		// 2. 添加附件（如果存在）
		if (attachments != null) {
			for (File attachment : attachments) {
				if (attachment == null)
					continue;
				MimeBodyPart attachmentPart = new MimeBodyPart();
				FileDataSource source = new FileDataSource(attachment);
				attachmentPart.setDataHandler(new DataHandler(source));
				// 处理文件名编码（防止中文乱码）
				attachmentPart.setFileName(MimeUtility.encodeText(attachment.getName(), "UTF-8", null));
				multipart.addBodyPart(attachmentPart);
			}
		}

        message.setContent(multipart);
        return message;
    }

    public enum SmtpServerEnum {
        EXMAIL_QQ("smtp.exmail.qq.com", "465"),
        GMAIL("smtp.gmail.com", "587");

        private String host;
        private String port;

		SmtpServerEnum(String host, String port) {
			this.host = host;
			this.port = port;
		}

		public String getHost() {
			return host;
		}

		public String getPort() {
			return port;
		}
    }
}
