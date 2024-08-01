package org.zhinanzhen.tb.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class UserDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date gmtCreate;

	private String name;

	private Date birthday;
	
	private String areaCode;

	private String phone;

	private String email;
	
	private String wechatUsername;

	private String firstControllerContents;

	private String visaCode;

	private Date visaExpirationDate;

	private String source;

	private String authType;

	private String authOpenid;

	private String authUsername;

	private String authNickname;

	private String authLogo;

	private double balance;

	private int regionId; // 即将废弃

	private int adviserId;

	private String recommendOpenid;

	private Date dob;

	private String stateText; // 添加方式

	private String channelSource; // 渠道来源
}
