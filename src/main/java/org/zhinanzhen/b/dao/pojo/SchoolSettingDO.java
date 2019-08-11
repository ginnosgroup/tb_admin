package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SchoolSettingDO {
	
	private int id;
	
	private String schoolName;
	
	private int type;
	
	private Date startDate;
	
	private Date endDate;
	
	private String parameters;

}
