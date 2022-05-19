package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.b.dao.pojo.ServiceAssessDO;
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
	
	private String stateMark;
	
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

	private double gst;

	private double deductGst;

	private double bonus;

	private int userId;

	private UserDTO user;
	
	private String applicantIds;
	
	private List<ApplicantDTO> applicantList;

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

	private List<MailRemindDTO> mailRemindDTOS;

	private SchoolInstitutionListDTO schoolInstitutionListDTO ;

}
