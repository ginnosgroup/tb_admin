package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class CommissionOrderDO {

	private int id;

	private Date gmtCreate;

	private String code;

	private int serviceOrderId;

	private String state;

	private String commissionState;

	private boolean isSettle;

	private boolean isDepositUser;

	private int schoolId;

	private String studentCode;

	private int userId;

	private int adviserId;

	private int officialId;

	private boolean isStudying;

	private int installmentNum;

	private int installment;

	private Date installmentDueDate;

	private String paymentVoucherImageUrl1;

	private String paymentVoucherImageUrl2;

	private Date startDate;

	private Date endDate;

	private double tuitionFee;

	private double perTermTuitionFee;

	private int receiveTypeId;

	private Date receiveDate;

	private double perAmount;

	private double amount;

	private double expectAmount;

	private double discount;

	private double gst;

	private double deductGst;

	private double bonus;

	private Date bonusDate;

	private String remarks;

	private boolean isClose;

}
