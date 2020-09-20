package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import lombok.Data;

@Data
public class ServiceOrderDTO {

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

	private ServicePackageDTO servicePackage;

	private int schoolId;

	private SchoolDTO school;

	private String state;
	
	private String auditingState;
	
	private Date maraApprovalDate;

	private Date officialApprovalDate;

	private boolean isSettle;

	private boolean isDepositUser;

	private boolean isSubmitted;

	private int subagencyId;

	private SubagencyDTO subagency;

	private boolean isPay;

	private int receiveTypeId;

	private ReceiveTypeDTO receiveType;

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

	private UserDTO user;

	private int maraId;

	private MaraDTO mara;

	private int adviserId;

	private AdviserDTO adviser;

	private int adviserId2;

	private AdviserDTO adviser2;

	private int officialId;

	private OfficialDTO official;

	private String remarks;

	private String closedReason;

	private boolean hasCommissionOrder = false;

	private ServiceOrderReviewDTO review;

	private List<ServiceOrderReviewDTO> reviews;

	private List<ChildrenServiceOrderDTO> childrenServiceOrders;
	
	private List<ServiceOrderOfficialRemarksDTO> officialNotes;

}
