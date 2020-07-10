package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

@Data
public class CommissionOrderReportDO {

	private String date;

	private int regionId;

	private String area;

	private int adviserId;

	private String consultant;

	private double deductionCommission;

	private double claimCommission;

	private double claimedCommission;

}
