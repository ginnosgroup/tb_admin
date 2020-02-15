package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class ServiceOrderDO {

	private int id;

	private String type;

	private int serviceId;

	private String state;

	private boolean isSettle;

	private boolean isDepositUser;

	private int subagencyId;

	private boolean isPay;

	private int receiveTypeId;

	private Date receiveDate;

	private double receivable;

	private double discount;

	private double received;

	private int paymentTimes;

	private double amount;

	private double gst;

	private double deductGst;

	private double bonus;

	private int userId;

	private int maraId;

	private int adviserId;

	private int officialId;

	private String remarks;

}
