package org.zhinanzhen.tb.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class AdminUserDTO {

	private int id;

	private String username;

	private String apList;

	private String sessionId;

	private Date gmtLogin;

	private String status;

}
