package org.zhinanzhen.tb.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class VirtualUserDTO {
	
	private int id;

	private Date gmtCreate;

	private String name;

	private String authNickname;

	private String authLogo;

}
