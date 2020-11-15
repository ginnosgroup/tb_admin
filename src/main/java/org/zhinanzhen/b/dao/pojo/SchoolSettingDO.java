package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class SchoolSettingDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	
	private int schoolId;

	private String schoolName;
	
	private String schoolSubject;

	private int type = 0;

	private Date startDate = new Date();

	private Date endDate = new Date();

	private String parameters;

}
