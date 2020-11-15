package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class VisaDTO {

	private int id;

	private Date gmtCreate;

	private String code;

	private Date handlingDate;

	private int userId;

	private String state;

	private String commissionState;

	private Date kjApprovalDate;

	private String userName;

	private Date birthday;

	private String phone;

	private int receiveTypeId;

	private String receiveTypeName;

	private Date receiveDate;

	private int serviceId;

	private int serviceOrderId;

	private ServiceOrderDTO serviceOrder;

	private String serviceCode;

	private int installmentNum;

	private int installment;

	private String paymentVoucherImageUrl1;

	private String paymentVoucherImageUrl2;

	private String paymentVoucherImageUrl3;

	private String paymentVoucherImageUrl4;

	private String paymentVoucherImageUrl5;

	private String visaVoucherImageUrl;

	private double receivable; // TODO: 准备废弃

	private double received; // TODO: 准备废弃

	private double perAmount;

	private double amount;

	private double expectAmount;

	private double sureExpectAmount;

	private double discount;
	
	private String invoiceNumber;

	private double gst;

	private double deductGst;

	private double bonus;

	private Date bonusDate;

	private int adviserId;

	private int maraId;

	private String adviserName;

	private int officialId;

	private String officialName;

	private String bankCheck;

	private boolean isChecked;

	private String remarks;

	private boolean isClose;

	private List<Date> remindDateList;

	private double totalPerAmount;

	private double totalAmount;

}
