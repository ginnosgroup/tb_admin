package org.zhinanzhen.tb.service.pojo;

import java.util.Date;

import org.zhinanzhen.tb.service.UserAuthTypeEnum;

import lombok.Data;

@Data
public class UserDTO {

	private int id;

	private Date gmtCreate;

	private String name;

	private String phone;

	private String email;

	private UserAuthTypeEnum authType;

	private String authOpenid;

	private String authUsername;

	private String authNickname;
	
	private String authLogo;

	private double balance;

	private int regionId;
	
	private int adviserId;
	
	private String recommendOpenid;
	
	private AdviserDTO adviserDto;
	    
	private UserDTO recommendUserDto;
}
