package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import org.zhinanzhen.b.service.pojo.ServiceDTO;

import lombok.Data;

@Data
public class ServiceOrderDO implements Serializable {

	private static final long serialVersionUID = 1L;

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
	
	private int applicantParentId;

	private int servicePackageId;

	private int schoolId;

	private String state;
	
	private String stateMark;
	
	private String stateMark2;
	
	private String auditingState;
	
	private String urgentState;

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

	private String invoiceVoucherImageUrl1;

	private String invoiceVoucherImageUrl2;

	private String invoiceVoucherImageUrl3;

	private String invoiceVoucherImageUrl4;

	private String invoiceVoucherImageUrl5;

	private String kjPaymentImageUrl1;

	private String kjPaymentImageUrl2;
	
	private String lowPriceImageUrl;

	private double perAmount;

	private double amount;

	private double expectAmount;
	
	private String currency = "AUD";
	
	private double exchangeRate = 4.80;

	private double gst;

	private double deductGst;

	private double bonus;

	private int userId;
	
	private int applicantId;

	private int maraId;

	private int adviserId;

	private int adviserId2;

	private int officialId;

	private String remarks;
	
	private String refuseReason;

	private String closedReason;

	private String information;

	private boolean isHistory;

	private  String nutCloud;

	private String serviceAssessId;

	private  Integer realPeopleNumber;

	//对账使用的code
	private String verifyCode;

	private Date readcommittedDate;
	
	private String refNo;

	private Date submitMaraApprovalDate;

	private int courseId;//新学校库专业id

	private int schoolInstitutionLocationId;//新学校库校区id

	private String institutionTradingName;//记录一下培训机构的名字

	private Integer EOINumber; // EOI数量

	private Integer bindingOrder; // 免费订单绑定的已支付订单

	private Double distributableAmount; // 订单可分配金额

	private String offerType; // offer类型

	private String offerUrl; // Offer文件路径

	private String expectTimeEnrollment; // 期望入学时间

	private Boolean isApplyVisa; // 是否后续办理学生签证(500)

	private String visaNumber; // 签证编号

	private String isInsuranceCompany; // 是否购买保险

	private String transferRemarks; // 中转备注

}
