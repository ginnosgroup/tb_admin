package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import org.zhinanzhen.b.service.pojo.ServiceDTO;

import lombok.Data;

@Data
public class ServiceOrderDO {

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private String code;

	private Date finishDate;

	private String type;

	private int peopleNumber;

	private String peopleType;

	private String peopleRemarks;

	private int serviceId;

	private ServiceDTO service;

	private int parentId;

	private int servicePackageId;

	private int schoolId;

	private String state;

	private Date maraApprovalDate;

	private Date officialApprovalDate;

	private String reviewState;

	private boolean isSettle;

	private boolean isDepositUser;

	private boolean isSubmitted;

	private int subagencyId;

	private boolean isPay;

	private int receiveTypeId;

	private Date receiveDate;

	private double receivable;

	private double discount;

	private double received;

	private int installment;

	private String paymentVoucherImageUrl1;

	private String paymentVoucherImageUrl2;

	private String paymentVoucherImageUrl3;

	private String paymentVoucherImageUrl4;

	private String paymentVoucherImageUrl5;

	private String coePaymentVoucherImageUrl1;

	private String coePaymentVoucherImageUrl2;

	private String coePaymentVoucherImageUrl3;

	private String coePaymentVoucherImageUrl4;

	private String coePaymentVoucherImageUrl5;

	private String visaVoucherImageUrl;

	private double perAmount;

	private double amount;

	private double expectAmount;

	private double gst;

	private double deductGst;

	private double bonus;

	private int userId;

	private int maraId;

	private int adviserId;

	private int adviserId2;

	private int officialId;

	private String remarks;

	private String closedReason;

}
