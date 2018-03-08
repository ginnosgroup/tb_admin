package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class RefundDTO {
	
	private int id;

	private Date gmtCreate;

	private Date handlingDate;

	private int userId;
	
	private String userName;
	
	private Date birthday;
	
	private String phone;

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
	
	private String officialName;
	
	private boolean isClose;

}
