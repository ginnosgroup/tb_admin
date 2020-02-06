package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class CommissionOrderDTO {

	private int id;

	private Date gmtCreate;
	
	private int serviceOrderId;

	private int installment;

	private Date startDate;

	private Date endDate;

	private double tuitionFee;

	private double perTermTuitionFee;

	private String remarks;

	private boolean isClose;

}
