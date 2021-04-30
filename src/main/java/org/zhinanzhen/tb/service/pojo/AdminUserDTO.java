package org.zhinanzhen.tb.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class AdminUserDTO {

	private int id;

	private String username;

	private String apList;
	
	private int adviserId;
	
	private int maraId;
	
	private int officialId;
	
	private int kjId;
	
	private Integer regionId;
	
	private boolean isOfficialAdmin;

	private String sessionId;

	private Date gmtLogin;

	private String status;

	private String operUserId;

}
