package org.zhinanzhen.b.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.zhinanzhen.b.service.pojo.ServiceOrderApplicantDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderJsonRequest {
    private Integer id;
    private String type;
    private String serviceType;
    private Integer peopleNumber;
    private String peopleType;
    private String peopleRemarks;
    private String serviceId;
    private Integer schoolId;
    private Integer schoolId2;
    private Integer schoolId3;
    private Integer schoolId4;
    private Integer schoolId5;
    private String urgentState;
    private String isSettle;
    private String isDepositUser;
    private String subagencyId;
    private String isPay;
    private String receiveTypeId;
    private String receiveDate;
    private String receivable;
    private String discount;
    private String received;
    private Integer installment;
    private String paymentVoucherImageUrl1;
    private String paymentVoucherImageUrl2;
    private String paymentVoucherImageUrl3;
    private String paymentVoucherImageUrl4;
    private String paymentVoucherImageUrl5;
    private String invoiceVoucherImageUrl1;
    private String invoiceVoucherImageUrl2;
    private String invoiceVoucherImageUrl3;
    private String invoiceVoucherImageUrl4;
    private String invoiceVoucherImageUrl5;
    private String kjPaymentImageUrl1;
    private String kjPaymentImageUrl2;
    private String lowPriceImageUrl;
    private String perAmount;
    private String amount;
    private String expectAmount;
    private String currency;
    private String exchangeRate;
    private String gst;
    private String deductGst;
    private String bonus;
    private String userId;
    private String applicantId;
    private String applicantBirthday;
    private String servicePackageIdsEOI;
    private String servicePackageIds;
    private String maraId;
    private String adviserId;
    private String officialId;
    private String remarks;
    private String information;
    private String closedReason;
    private String isHistory;
    private String nutCloud;
    private String serviceAssessId;
    private String verifyCode;
    private String refNo;
    private Integer courseId;
    private Integer courseId2;
    private Integer courseId3;
    private Integer courseId4;
    private Integer courseId5;
    private Integer schoolInstitutionLocationId;
    private Integer schoolInstitutionLocationId2;
    private Integer schoolInstitutionLocationId3;
    private Integer schoolInstitutionLocationId4;
    private Integer schoolInstitutionLocationId5;
    private String institutionTradingName;
    private String institutionTradingName2;
    private String institutionTradingName3;
    private String institutionTradingName4;
    private String institutionTradingName5;
    private Integer bindingOrder;
    private String expectTimeEnrollment;
    private Boolean isApplyVisa;
    private String visaNumber;
    private String insuranceCompany; // 保险公司id
    private String hasInsurance; // 是否购买保险
    private String isTransfer; // 是否为中转订单
    private String transferRemarks; // 是否为中转订单
    private String offerUrl; // 是否为中转订单
    private String offerType; // 是否为中转订单
    private String officialData;
    private String scoreOptions;
    private String scoreState;
    private String scoreMark; // 评分备注
    private String serviceAssessCategoryId;
    private Integer bindingOrderId;
    private Integer manageId;
    private String serviceOrderApplicantList;
    private String contractData;
    private String eoiType; // EOI类型
    private String isCOE;
    private String isCOE1;
    private String isCOE2;
    private String isCOE3;
    private String isCOE4;
    private String isCOE5;
}
