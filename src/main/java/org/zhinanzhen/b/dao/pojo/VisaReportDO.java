package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

@Data
public class VisaReportDO {
	
	private String date;
	
	private int regionId;
	
	private String area;
	
	private int adviserId;
	
	private String consultant;
	
	private double commission;
	
	private double serviceFee;

}
