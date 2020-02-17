package org.zhinanzhen.tb.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class AdminUserDO {

	private int id;

	private String username;

	private String password;

	private String apList;

	private int adviserId;

	private int maraId;

	private int officialId;

	private String sessionId;

	private Date gmtLogin;

	private String status;

}
