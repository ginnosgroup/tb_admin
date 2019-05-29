package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class RefundDO {

	private int id;

	private Date gmtCreate;

	private Date handlingDate;

	private int userId;
	
	private String name;

	private int receiveTypeId;

	private double amount;

	private double preRefundAmount;

	private String bankName;

	private String bankAccount;

	private String bsb;

	private Date refundDate;

	private double refundAmount;

	private double gst;

	private double deductGst;

	private double refund;

	private int adviserId;

	private int officialId;
	
	private String remarks;
	
	private boolean isClose;

}
