package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class RefundDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private String state;

	private String type;
	
	private Integer visaId;

	private Integer commissionOrderId;

	private int userId;
	
	private String userName;

	private int adviserId;
	
	private String adviserName;

	private int maraId;

	private int officialId;
	
	private String officialName;

	private int schoolId;

	private int courseId;

	private Date receiveDate;

	private double received;
	
	private double amount;

	private String paymentVoucherImageUrl;

	private String refundVoucherImageUrl;

	private int refundDetailId;

	private String refundDetail;

	private String currencyType;

	private String accountName;

	private String bankName;

	private String bsb;
	
	private String rmbRemarks;
	
	private String reason;

	private String remarks;

}
