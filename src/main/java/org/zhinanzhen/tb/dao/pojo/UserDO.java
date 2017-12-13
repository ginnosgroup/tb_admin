package org.zhinanzhen.tb.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class UserDO {

	private int id;
	
	private Date gmtCreate;

	private String name;

	private String phone;

	private String email;

	private String authType;

	private String authOpenid;
	
	private String authUsername;

	private String authNickname;
	
	private String authLogo;

	private double balance;

	private int regionId;
	
	private int adviserId;
	
	private String recommendOpenid;
}
