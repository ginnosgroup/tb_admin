package org.zhinanzhen.b.dao.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zhinanzhen.b.service.pojo.ServiceOrderApplicantDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderManageRequest {
    private Integer id;
    private String type;
    private Integer peopleNumber;
    private String peopleType;
    private String peopleRemarks;
    private String serviceId;
    private Integer schoolId;
    private Integer schoolId2;
    private Integer schoolId3;
    private Integer schoolId4;
    private Integer schoolId5;
    private String servicePackageIds;
    private String servicePackageIdsEOI;
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
    // 修改这里：从String改为List
    private List<ServiceOrderApplicantDTO> serviceOrderApplicantList;
    private String maraId;
    private String adviserId;
    private String officialId;
    private String remarks;
    private String closedReason;
    private String information;
    private String isHistory;
    private String nutCloud;
    private String serviceAssessId;
    private String verifyCode;
    private String refNo;
    private Integer courseId;
    private Integer schoolInstitutionLocationId;
    private Integer courseId2;
    private Integer schoolInstitutionLocationId2;
    private Integer courseId3;
    private Integer schoolInstitutionLocationId3;
    private Integer courseId4;
    private Integer schoolInstitutionLocationId4;
    private Integer courseId5;
    private Integer schoolInstitutionLocationId5;
    private String institutionTradingName;
    private String institutionTradingName2;
    private String institutionTradingName3;
    private String institutionTradingName4;
    private String institutionTradingName5;
    private Integer bindingOrderId;
    private String expectTimeEnrollment;
    private Boolean isApplyVisa;
    private String visaNumber;
    private String serviceAssessCategoryId;
    private String scoreOptions;
    private List<ServiceOrderJsonRequest> serviceOrderJson;

}