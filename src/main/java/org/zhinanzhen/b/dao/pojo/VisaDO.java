package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class VisaDO {

	private int id;

	private Date gmtCreate;
	
	private String code;

	private Date handlingDate;

	private int userId;

	private String state;

	private int receiveTypeId;

	private Date receiveDate;

	private int serviceId;

	private int serviceOrderId;

	private int installmentNum;

	private int installment;

	private double receivable;

	private double received;

	private double perAmount;

	private double amount;

	private double expectAmount;

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
