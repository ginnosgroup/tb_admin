package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class VisaReportDO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String date;
	
	private int regionId;
	
	private String area;
	
	private int adviserId;
	
	private String consultant;
	
	private String currency;
	
	private double exchangeRate;
	
	private double amount;
	
	private double commission;
	
	private double serviceFee;

}
