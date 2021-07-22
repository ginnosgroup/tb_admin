package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
public class MailLogDO implements Serializable {

	private static final long serialVersionUID = 1L;

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
