package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class RefundDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date gmtCreate;
	
	private Date reviewedDate;
	
	private Date completedDate;

	private String state;

	private String type;
	
	private Integer visaId;

	private Integer commissionOrderId;

	private int userId;
	
	private int applicantId;
	
	private String userName;

	private int adviserId;
	
	private String adviserName;

	private String adviserRegionName;

	private int maraId;

	private int officialId;
	
	private String officialName;

	private int schoolId;

	private int courseId;

	private Date receiveDate;

	private double received;
	
	private double amount;

	private String paymentVoucherImageUrl;
	
	private String paymentVoucherImageUrl2;
	
	private String paymentVoucherImageUrl3;
	
	private String paymentVoucherImageUrl4;
	
	private String paymentVoucherImageUrl5;

	private String refundVoucherImageUrl;

	private int refundDetailId;

	private String refundDetail;

	private String currencyType;
	
	private double exchangeRate;

	private String accountName;

	private String bankName;

	private String bsb;
	
	private String rmbRemarks;
	
	private String reason;

	private String remarks;
	
	private String note;

	private double amountCNY; // 收款银行人民币金额
}
