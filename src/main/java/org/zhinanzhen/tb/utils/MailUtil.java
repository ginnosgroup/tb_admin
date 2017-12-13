package org.zhinanzhen.tb.utils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.util.MailSSLSocketFactory;

public class MailUtil {

	private static final Logger LOG = LoggerFactory.getLogger(MailUtil.class);

	private static final String SMTP_HOST = "smtp.exmail.qq.com";

	private static final String USERNAME = "system@zhinanzhen.org";

	private static final String PASSWORD = "Znz2017";

	public static boolean sendMail(String toMail, String subject, String content) {

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.host", SMTP_HOST);
		props.setProperty("mail.smtp.auth", "true");

		MailSSLSocketFactory sf = null;
		try {
			sf = new MailSSLSocketFactory();
			sf.setTrustAllHosts(true);
		} catch (GeneralSecurityException e1) {
			e1.printStackTrace();
		}
		props.put("mail.smtp.ssl.enable", "true");
		props.put("mail.smtp.ssl.socketFactory", sf);

		Session session = Session.getDefaultInstance(props);
		// session.setDebug(true);

		MimeMessage message = new MimeMessage(session);

		Transport transport = null;

		try {
			message.setFrom(new InternetAddress(USERNAME, "System", "UTF-8"));
			message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toMail, "", "UTF-8"));
			// message.setRecipient(MimeMessage.RecipientType.BCC, new
			// InternetAddress("leisu@zhinanzhen.org", "", "UTF-8"));
			message.setSubject(subject, "UTF-8");
			message.setContent(content, "text/html;charset=UTF-8");
			message.saveChanges();
			transport = session.getTransport();
			transport.connect(USERNAME, PASSWORD);
			transport.sendMessage(message, message.getAllRecipients());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
			return false;
		} catch (MessagingException e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
			return false;
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
				}
			}
		}
		return true;
	}

}
