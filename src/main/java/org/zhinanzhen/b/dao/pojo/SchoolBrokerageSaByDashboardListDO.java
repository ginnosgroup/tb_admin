package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SchoolBrokerageSaByDashboardListDO {

	private int id;

	private Date gmtCreate;

	private Date handlingDate;

	private int userId;

	private String userName;

	private Date birthday;

	private String phone;

	private int schoolId;

	private String schoolName;

	private int adviserId;

	private String adviserName;

	private int officialId;

	private String officialName;

}
