package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

@Data
public class AdviserCommissionOrderDO {

	private int id;

	private String gmtCreate;

	private String serviceOrderId;

	private String userName;
	
	private String applicantName;

	private String officialName;

	private String kjApprovalDate;

	private String isSettle;

	private String studentCode;

	private String schoolPaymentAmount;

	private String schoolPaymentDate;

	private String tuitionFee;

	private String perTermTuitionFee;

	private String receiveTypeName;

	private String receiveDate;

	private String perAmount;

	private String amount;

	private String expectAmount;

	private String sureExpectAmount;

	private String gst;

	private String bonus;

	private String bonusDate;

	private String commissionState;

	private String remarks;

}
