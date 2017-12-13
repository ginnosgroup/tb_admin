package org.zhinanzhen.tb.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class AdviserDO {

	private int id;
	
	private Date gmtCreate;

	private String name;

	private String phone;

	private String email;
	
	private String state;
	
	private String imageUrl;

	private int regionId;

}
