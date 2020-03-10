package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class VisaDTO {

	private int id;

	private Date gmtCreate;

	private Date handlingDate;

	private int userId;
	
	private String state;

	private String userName;

	private Date birthday;

	private String phone;

	private int receiveTypeId;

	private String receiveTypeName;

	private Date receiveDate;

	private int serviceId;

	private int serviceOrderId;

	private String serviceCode;

	private double receivable;

	private double received;
	
	private double perAmount;

	private double amount;

	private double discount;

	private double gst;

	private double deductGst;

	private double bonus;

	private int adviserId;

	private int maraId;

	private String adviserName;

	private int officialId;

	private String officialName;

	private String remarks;

	private boolean isClose;

	private List<Date> remindDateList;

}
