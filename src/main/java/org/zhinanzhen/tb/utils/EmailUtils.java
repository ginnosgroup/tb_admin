package org.zhinanzhen.tb.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;  
import javax.mail.internet.MimeMessage;  
import java.util.Properties;  
  
public class EmailUtils {  
  
    /**  
     * 发送邮件的方法  
     *  
     * @param host SMTP服务器地址  
     * @param port SMTP服务器端口  
     * @param username 发件人邮箱地址  
     * @param password 发件人邮箱密码  
     * @param to 收件人邮箱地址  
     * @param subject 邮件主题  
     * @param content 邮件正文  
     * @param isHtml 是否为HTML格式邮件  
     * @throws MessagingException 邮件发送异常  
     */  
    public static void sendEmail(String host, int port, String username, String password, String to,  
                                 String subject, String content, boolean isHtml) throws MessagingException {  
        // 创建邮件会话  
        Properties props = new Properties();  
        props.put("mail.smtp.host", host);  
        props.put("mail.smtp.port", port);  
        props.put("mail.smtp.auth", "true");  
        // 根据需要开启SSL加密连接  
        // props.put("mail.smtp.ssl.enable", "true");  
  
        // 创建邮件会话  
        Session session = Session.getInstance(props,  
                new javax.mail.Authenticator() {  
                    protected PasswordAuthentication getPasswordAuthentication() {  
                        return new PasswordAuthentication(username, password);  
                    }  
                });  
  
        // 创建邮件对象  
        MimeMessage message = new MimeMessage(session);  
  
        // 设置发件人  
        message.setFrom(new InternetAddress(username));  
  
        // 设置收件人  
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));  
  
        // 设置邮件主题  
        message.setSubject(subject);  
  
        // 设置邮件正文  
        if (isHtml) {  
            message.setContent(content, "text/html;charset=UTF-8");  
        } else {  
            message.setText(content, "UTF-8");  
        }  
  
        // 发送邮件  
        Transport.send(message);  
    }  
  
    // 其他辅助方法...  
}
