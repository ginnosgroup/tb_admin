package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.ServicePackagePriceDO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author: HL
 * @CreateTime: 2022-09-27
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class VisaOfficialDTO {

    private int id;

    private Date gmtCreate;

    private String code;

    private Date handlingDate;

    private int userId;

    private UserDTO user;

    private int applicantId;

    private List<ApplicantDTO> applicant;

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

    private double receivable; // TODO:sulei 准备废弃

    private double received; // TODO:sulei 准备废弃

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

    private String invoiceNumber;

    private double gst;

    private double gstAUD;

    private double deductGst;

    private double deductGstAUD;

    private double bonus;

    private double bonusAUD;

    private Date bonusDate;

    private int adviserId;

    private int maraId;

    private String adviserName;

    private int officialId;

    private String officialName;

    private String bankCheck;

    private boolean isChecked;
    
    private boolean isMerged;

    private String remarks;

    private String refuseReason;

    private String verifyCode;

    private Date bankDate;

    private boolean isClose;

    private List<Date> remindDateList;

    private double totalPerAmount;

    private double totalPerAmountAUD;

    private double totalPerAmountCNY;

    private double totalAmount;

    private double totalAmountAUD;

    private double totalAmountCNY;

    private Date invoiceCreate;

    private boolean isRefunded = false;

    private List<MailRemindDTO> mailRemindDTOS;

    private Date submitIbDate;//提交移民局时间

    private Double commissionAmount; //计入佣金提点金额(确认)

    private Double expectCommissionAmount;//预计计入佣金提点金额

    private Double predictCommission; //预估佣金

    private double refund;//退款

    private double rate;

    private String calculation;//计算规则

    private double predictCommissionAmount;//计入佣金提点金额(预估)

    private ServicePackagePriceDO servicePackagePriceDO;

    private MaraDTO maraDTO;

    private Integer officialRegion;

    private Double predictCommissionCNY;

    private String sortEOI;

    private Boolean isRefund = false;

    private Double refundAmount; // 退款金额

    private Double bingDingAmount; // 绑定金额

}

