package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

@Data
public class AdviserServiceOrderDO {

	private int id;

	private String gmtCreate;

	private String type;

	private String userName;
	
	private String applicantName;

	private String maraName;

	private String officialName;
	
	private String currency;
	
	private double exchangeRate;

	private String receiveTypeName;

	private String serviceOrderReceiveDate;
	
	private String serviceCode;
	
	private String serviceName;
	
	private String serviceAssessId;
	
	private int servicePackageId;
	
	private String institutionTradingName;
	
	private int courseId;
	
	private int schoolId;
	
	private boolean isSubmitted;

	private String serviceOrderState;

	private String isSettle;

	private String perAmount;

	private String amount;

	private String expectAmount;

	private String remarks;

}
