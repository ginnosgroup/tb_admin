package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class OrderRefundDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private String state;
	
	private String type;

	private Integer visaId;

	private Integer commissionOrderId;
	
	private Date receiveDate;

	private double received;
	
	private String paymentVoucherImageUrl;
	
	private String refundVoucherImageUrl;

	private int refundDetailId;

	private String refundDetail;

	private String currencyType;

	private String amountName;

	private String bankName;

	private String bsb;

	private String remarks;

}
