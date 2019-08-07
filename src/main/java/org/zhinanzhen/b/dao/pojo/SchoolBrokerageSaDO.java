package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SchoolBrokerageSaDO {

	private int id;

	private Date gmtCreate;

	private Date handlingDate;

	private int userId;

	private int schoolId;

	private String studentCode;

	private Date startDate;

	private Date endDate;

	private double tuitionFee;

	private double firstTermTuitionFee;
	
	private double discount;

	private double commission;

	private double gst;

	private double deductGst;

	private double bonus;

	private Date payDate;

	private String invoiceCode;

	private double payAmount;

	private int subagencyId;

	private int adviserId;

	private int officialId;
	
	private String remarks;

	private boolean isSettleAccounts = false;

	private boolean isClose;

}
