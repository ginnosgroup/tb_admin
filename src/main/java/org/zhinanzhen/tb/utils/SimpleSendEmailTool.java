package org.zhinanzhen.tb.utils;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

@AllArgsConstructor
public class SimpleSendEmailTool {

	private String from = null;
	private String password = null;
	private SmtpServerEnum smtpServer = null;

	@SneakyThrows
	public void send(String mail, String title, String text) {

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
		Message message = createSimpleMail(session, from, mail, title, text);
		ts.sendMessage(message, message.getAllRecipients());
		ts.close();

	}

	private MimeMessage createSimpleMail(Session session, String mailfrom, String mailTo, String mailTittle,
			String mailText) throws Exception {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(mailfrom));
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
		message.setSubject(mailTittle);
		message.setContent(mailText, "text/html;charset=UTF-8");
		return message;
	}

	@AllArgsConstructor
	public enum SmtpServerEnum {

		EXMAIL_QQ("smtp.exmail.qq.com", "587"), GMAIL("smtp.gmail.com", "587");

		@Getter
		private String host;
		@Getter
		private String port;

	}

}
