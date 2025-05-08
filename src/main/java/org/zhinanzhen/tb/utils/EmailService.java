package org.zhinanzhen.tb.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Properties;

@AllArgsConstructor
@NoArgsConstructor
public class EmailService {
    private String from = null;
    private String password = null;
    private SmtpServerEnum smtpServer = null;

    // 发送带附件邮件的方法（重载）
    @SneakyThrows
    public void send(String mail, String title, String text, File attachment) {
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
        Message message = createAttachmentMail(session, from, mail, title, text, attachment);
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();
    }

    // 原方法保持不变（发送纯文本）
    @SneakyThrows
    public void send(String mail, String title, String text) {
        this.send(mail, title, text, null); // 调用重载方法，附件为 null
    }

    // 创建带附件的邮件内容
    private MimeMessage createAttachmentMail(Session session, String mailFrom, String mailTo, String title,
                                                    String text, File attachment) throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailFrom));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
        message.setSubject(title);

        // 创建 Multipart 容器（文本 + 附件）
        Multipart multipart = new MimeMultipart();

        // 1. 添加文本内容
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(text, "text/html;charset=UTF-8");
        multipart.addBodyPart(textPart);

        // 2. 添加附件（如果存在）
        if (attachment != null) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(attachment);
            attachmentPart.setDataHandler(new DataHandler(source));
            // 处理文件名编码（防止中文乱码）
            attachmentPart.setFileName(MimeUtility.encodeText(attachment.getName()));
            multipart.addBodyPart(attachmentPart);
        }

        message.setContent(multipart);
        return message;
    }

    @AllArgsConstructor
    public enum SmtpServerEnum {
        EXMAIL_QQ("smtp.exmail.qq.com", "465"),
        GMAIL("smtp.gmail.com", "587");

        @Getter
        private String host;
        @Getter
        private String port;
    }
}