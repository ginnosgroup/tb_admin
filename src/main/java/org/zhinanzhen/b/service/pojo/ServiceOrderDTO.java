package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ServiceAssessDO;
import org.zhinanzhen.b.dao.pojo.VisaDO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import java.util.Date;
import java.util.List;

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
	
	private int applicantParentId;

	private String eoiList;

	private int servicePackageId;

	private ServicePackageDTO servicePackage;

	private int schoolId;

	private SchoolDTO school;

	private String state;
	
	private String stateMark;
	
	private String stateMark2;
	
	private String auditingState;
	
	private String urgentState;
	
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
	
	private double receivableAUD;
	
	private double receivableCNY;

	private double discount;
	
	private double discountAUD;

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
	
	private double perAmountAUD; // 本次应收(澳币)

	private double perAmountCNY; // 本次应收(人民币)

	private double amount;
	
	private double amountAUD; // 本次收款(澳币)

	private double amountCNY; // 本次收款(人民币)

	private double expectAmount;
	
	private double expectAmountAUD;

	private double expectAmountCNY;

	private String currency;

	private double exchangeRate;

	private double gst;
	
	private double gstAUD;

	private double deductGst;
	
	private double deductGstAUD;

	private double bonus;
	
	private double bonusAUD;

	private int userId;

	private UserDTO user;
	
	private int applicantId;
	
	private ApplicantDTO applicant;

	private int maraId;

	private MaraDTO mara;

	private int adviserId;

	private AdviserDTO adviser;

	private int adviserId2;

	private AdviserDTO adviser2;

	private int officialId;

	private OfficialDTO official;

	private String remarks;
	
	private String refuseReason;

	private String closedReason;

	private String information;

	private boolean isHistory;

	private  String nutCloud;

	private String serviceAssessId;

	private Integer realPeopleNumber;

	private String verifyCode;//会计自动对账code

	private Date readcommittedDate;
	
	private String refNo;

	private Date submitMaraApprovalDate;

	private int courseId;//新学校库专业id

	private int schoolInstitutionLocationId;//新学校库校区id

	private String institutionTradingName;//记录一下培训机构的名字

	private boolean hasCommissionOrder = false;

	private ServiceOrderReviewDTO review;
	
	private OfficialTagDTO officialTag;

	private List<ServiceOrderReviewDTO> reviews;

	private List<ChildrenServiceOrderDTO> childrenServiceOrders;
	
	private List<ServiceOrderOfficialRemarksDTO> officialNotes;

	private ServiceAssessDO serviceAssessDO;
	
	private List<Integer> cIds; // 佣金订单编号

	private List<VisaDO> visaDOList;

	private List<MailRemindDTO> mailRemindDTOS;

	private SchoolInstitutionListDTO schoolInstitutionListDTO ;

	private List<CommissionOrderDO> commissionOrderDTOList;

	private OfficialDO oldOfficial;

	private boolean isCreateVisaOffice;

	private boolean isSubmitMM;

	private String sortEOI;

	private Integer EOINumber; // EOI数量

	private Integer bindingOrder; // 免费订单绑定的已支付订单

	private Double distributableAmount; // 订单可分配金额

	private Long bindingOrderCount; // 可以绑定订单数量

	private String offerType; // offer类型

	private String offerUrl; // Offer文件路径

	private List<String> offerUrls;

	private String expectTimeEnrollment; // 期望入学时间

	private Boolean isApplyVisa; // 是否后续办理学生签证(500)

	private String visaNumber; // 签证编号

	private String visaCertificate; // 签证凭证路径

	private String visaStatus; // 签证状态

	private String visaStatusSub; // 签证类型
}
