package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;
import lombok.ToString;
import org.zhinanzhen.b.dao.pojo.MaraDO;
import org.zhinanzhen.b.dao.pojo.ServicePackagePriceDO;

@Data
@ToString
public class CommissionOrderDTO {

	private int id;

	private Date gmtCreate;

	private String code;

	private int serviceOrderId;

	private String state;

	private String commissionState;

	private Date kjApprovalDate;

	private boolean isSettle;

	private boolean isDepositUser;

	private int schoolId;

	private int courseId;

	private int schoolInstitutionLocationId;

	private String studentCode;

	private int userId;
	
	private int applicantId;

	private ApplicantDTO applicant;

	private int adviserId;

	private int officialId;

	private boolean isStudying;

	private int installmentNum;

	private int installment;

	private Date installmentDueDate;

	private String paymentVoucherImageUrl1;

	private String paymentVoucherImageUrl2;

	private String paymentVoucherImageUrl3;

	private String paymentVoucherImageUrl4;

	private String paymentVoucherImageUrl5;

	private double schoolPaymentAmount;

	private Date schoolPaymentDate;

	private String invoiceNumber;

	private Date dob;

	private Date startDate;

	private Date endDate;

	private double commission;

	private double tuitionFee;

	private double perTermTuitionFee;

	private int receiveTypeId;

	private Date receiveDate;

	private double perAmount; // 本次应收
	
	private double perAmountAUD; // 本次应收(澳币)
	
	private double perAmountCNY; // 本次应收(人民币)

	private double amount; // 本次收款
	
	private double amountAUD; // 本次收款(澳币)
	
	private double amountCNY; // 本次收款(人民币)

	private double expectAmount;

	private double expectAmountAUD;

	private double expectAmountCNY;

	private double sureExpectAmount = 0.00;

	private double sureExpectAmountAUD;

	private double sureExpectAmountCNY;
	
	private String currency;
	
	private double exchangeRate;

	private double discount;
	
	private double discountAUD;

	private double gst;
	
	private double gstAUD;

	private double deductGst;
	
	private double deductGstAUD;

	private double bonus;
	
	private double bonusAUD;

	private Date bonusDate;

	private Date zyDate;

	private String bankCheck;

	private boolean isChecked;

	private String remarks;

	private String refuseReason;

	private boolean isClose;

	private String applyState;

	private String verifyCode;

	private Date bankDate;

	private Date invoiceCreate;

	private Date submitIbDate;//提交移民局时间

	private Double commissionAmount; //计入佣金提点金额(确认)

	private Double expectCommissionAmount;//预计计入佣金提点金额

	private Double estimatedCommission; //预估佣金

	private double refund;//退款

	private double rate;

	private ServicePackagePriceDO servicePackagePriceDO;

	private String visaCertificate; // 签证凭证路径

	private String visaStatus; // 签证状态

	private String visaStatusSub; // 签证类型

}
