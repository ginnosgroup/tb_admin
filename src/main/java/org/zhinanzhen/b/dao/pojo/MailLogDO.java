package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class MailLogDO {

	int id;

	Date gmtCreate;

	Date gmtModify;

	String code;

	String mail;

	String title;

	String content;

	public MailLogDO(String code, String mail, String title, String content) {
		this.code = code;
		this.mail = mail;
		this.title = title;
		this.content = content;
	}

}
