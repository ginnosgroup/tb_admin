package org.zhinanzhen.b.service.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MailLogDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	int id;

	Date gmtCreate;

	Date gmtModify;

	String code;

	String mail;

	String title;

	String content;

	public MailLogDTO(String code, String mail, String title, String content) {
		this.code = code;
		this.mail = mail;
		this.title = title;
		this.content = content;
	}

}
