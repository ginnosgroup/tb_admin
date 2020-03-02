package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class VisaDO {

	private int id;

	private Date gmtCreate;
	
	private Date handlingDate;

	private int userId;
	
	private String state;

	private int receiveTypeId;

	private Date receiveDate;

	private int serviceId;
	
	private int serviceOrderId;

	private double receivable;

	private double received;

	private double amount;
	
	private double discount;

	private double gst;

	private double deductGst;

	private double bonus;

	private int adviserId;
	
	private int maraId;

	private int officialId;
	
	private String remarks;
	
	private boolean isClose;

}
