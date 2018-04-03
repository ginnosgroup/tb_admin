package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class BrokerageSaDTO {

	private int id;

	private Date gmtCreate;

	private Date handlingDate;

	private int userId;

	private String userName;

	private Date birthday;

	private String phone;

	private int receiveTypeId;

	private int schoolId;

	private String schoolName;

	private String schoolSubject;

	private Date startDate;

	private Date endDate;

	private double tuitionFee;

	private double commission;

	private double gst;

	private double deductGst;

	private double bonus;

	private int adviserId;

	private String adviserName;

	private int officialId;
	
	private String officialName;

	private boolean isClose;

}
