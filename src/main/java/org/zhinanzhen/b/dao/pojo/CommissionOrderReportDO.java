package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class CommissionOrderReportDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String date;

	private int regionId;

	private String area;

	private int adviserId;

	private String consultant;

	private double deductionCommission;

	private double claimCommission;

	private double claimedCommission;

	private double adjustments;
	
	private double refunded;

}
