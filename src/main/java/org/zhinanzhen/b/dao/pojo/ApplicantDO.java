package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class ApplicantDO {

	private int id;

	private String gmtCreate;

	private String gmtModify;

	private String surname;

	private String firstname;

	private Date birthday;

	private String type;

	private String visaCode;

	private Date visaExpirationDate;

	private String nutCloud;

	private String fileUrl;

	private String firstControllerContents;

	private int userId;

}
