package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SchoolSettingDO {

	private int id;
	
	private int schoolId;

	private String schoolName;
	
	private String schoolSubject;

	private int type = 0;

	private Date startDate = new Date();

	private Date endDate = new Date();

	private String parameters;

}
