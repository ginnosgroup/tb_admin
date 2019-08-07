package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class SchoolBrokerageSaDTO {

	private int id;

	private Date gmtCreate;

	private Date handlingDate;

	private int userId;

	private String userName;

	private Date birthday;

	private String phone;

	private int schoolId;

	private String schoolName;

	private String schoolSubject;

	private String studentCode;

	private Date startDate;

	private Date endDate;
	
	private List<Date> remindDateList;

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

	private String subagencyName;

	private int adviserId;

	private String adviserName;

	private int officialId;
	
	private String officialName;
	
	private String remarks;

	private boolean isSettleAccounts;

	private boolean isClose;

}
