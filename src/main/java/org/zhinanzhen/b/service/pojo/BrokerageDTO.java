package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class BrokerageDTO {

	private int id;

	private Date gmtCreate;

	private Date handlingDate;

	private int userId;

	private String userName;

	private Date birthday;

	private String phone;

	private int receiveTypeId;
	
	private String receiveTypeName;

	private Date receiveDate;

	private int serviceId;
	
	private String serviceCode;

	private double receivable;

	private double received;

	private double amount;

	private double gst;

	private double deductGst;

	private double bonus;

	private int adviserId;

	private String adviserName;

	private int officialId;
	
	private String officialName;
	
	private String remarks;

	private boolean isClose;

}
