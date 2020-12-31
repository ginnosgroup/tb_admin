package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class CommissionOrderDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date gmtCreate;

	private String code;

	private int serviceOrderId;

	private String state;

	private String commissionState;
	
	private Date kjApprovalDate;

	private boolean isSettle;

	private boolean isDepositUser;

	private int schoolId;

	private String studentCode;

	private int userId;

	private int adviserId;

	private int officialId;

	private boolean isStudying;

	private int installmentNum;

	private int installment;

	private Date installmentDueDate;

	private String paymentVoucherImageUrl1;

	private String paymentVoucherImageUrl2;

	private String paymentVoucherImageUrl3;

	private String paymentVoucherImageUrl4;

	private String paymentVoucherImageUrl5;

	private double schoolPaymentAmount;

	private Date schoolPaymentDate;

	private String invoiceNumber;

	private Date dob;

	private Date startDate;

	private Date endDate;

	private double commission;

	private double tuitionFee;

	private double perTermTuitionFee;

	private int receiveTypeId;

	private Date receiveDate;

	private double perAmount;

	private double amount;

	private double expectAmount;
	
	private double sureExpectAmount;

	private double discount;

	private double gst;

	private double deductGst;

	private double bonus;

	private Date bonusDate;

	private Date zyDate;

	private String bankCheck;

	private boolean isChecked;

	private String remarks;

	private boolean isClose;

	private String applyState;

	private String verifyCode;

	private Date bankDate;
}
