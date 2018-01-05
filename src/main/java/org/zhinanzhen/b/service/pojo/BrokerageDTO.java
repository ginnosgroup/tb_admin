package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class BrokerageDTO {
	
	private int id;

	private Date gmtCreate;

	private int userId;

	private int receiveTypeId;

	private Date receiveDate;

	private int serviceId;

	private double receivable;

	private double received;

	private double amount;

	private double gst;

	private double deductGst;

	private double bonus;

	private double adviserId;

	private double officialId;
	
	private boolean isClose;

}
