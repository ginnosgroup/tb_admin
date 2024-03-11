package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

@Data
public class AdviserUserDO {

	private int id;

	private String name;
	
	private String nickname;
	
	private String weichatUsername;
	
	private int applicantId;
	
	private String applicantName;

	private String birthday;

	private String phone;

	private String email;

	private String isCreater;

}
