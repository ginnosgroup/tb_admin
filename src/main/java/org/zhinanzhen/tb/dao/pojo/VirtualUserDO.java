package org.zhinanzhen.tb.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class VirtualUserDO {

	private int id;

	private Date gmtCreate;

	private String name;

	private String authNickname;

	private String authLogo;

}
