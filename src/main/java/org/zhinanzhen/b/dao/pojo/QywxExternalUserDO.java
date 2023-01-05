package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class QywxExternalUserDO {
	
	private int id;
	
	private Date gmtCreate;
	
	private int adviserId;
	
	private String name;
	
	private int gender;
	
	private int type;
	
	private String avatar;
	
	private String externalUserid;
	
	private String state;
	
	private int userId;
	
	private int applicantId;
	
	private String phone;
	
	private String email;
	
	private String wechatUsername;
	
	private String weiboUsername;
	
	private String qq;

}
