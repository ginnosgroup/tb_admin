package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class CommissionOrderDO {

	private int id;

	private Date gmtCreate;

	private int installment;

	private Date startDate;

	private Date endDate;

	private double tuitionFee;

	private double perTermTuitionFee;

	private String remarks;

	private boolean isClose;

}
