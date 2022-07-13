package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

@Data
public class AdviserVisaDO {

	private int id;

	private String gmtCreate;

	private String serviceOrderId;

	private String userName;
	
	private String applicantName;

	private String maraName;

	private String officialName;

	private String kjApprovalDate;

	private String receiveTypeName;

	private String receiveDate;

	private String serviceCode;

	private String gst;

	private String bonus;

	private String bonusDate;

	private String commissionState;

	private String remarks;

}
