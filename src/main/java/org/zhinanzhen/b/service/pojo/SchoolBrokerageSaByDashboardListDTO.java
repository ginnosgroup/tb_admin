package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class SchoolBrokerageSaByDashboardListDTO {

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

	private List<Date> remindDateList;

}
