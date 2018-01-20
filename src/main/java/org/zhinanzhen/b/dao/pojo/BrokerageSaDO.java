package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class BrokerageSaDO {
	
	private int id;

	private Date gmtCreate;
	
	private int userId;
	
	private int receiveTypeId;
	
	private int schoolId;
	
	private int subjectId;
	
	private Date startDate;
	
	private Date endDate;
	
	private double tuitionFee;
	
	private double commission;
	
	private double gst;
	
	private double deductGst;
	
	private double bonus;
	
	private int adviserId;
	
	private int officialId;
	
	private boolean isClose;

}
