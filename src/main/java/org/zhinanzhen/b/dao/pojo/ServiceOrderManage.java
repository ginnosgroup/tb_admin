package org.zhinanzhen.b.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderManage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Date gmtCreate;
    private Date gmtModify;
    private String code;
    private Date finishDate;
    private Boolean isSubmitted;
    private Boolean isPay;
    private Integer receiveTypeId;
    private Date receiveDate;
    private Double receivable;
    private Double discount;
    private Double received;
    private Integer installment;
    private String paymentVoucherImageUrl1;
    private String paymentVoucherImageUrl2;
    private String paymentVoucherImageUrl3;
    private String paymentVoucherImageUrl4;
    private String paymentVoucherImageUrl5;
    private String lowPriceImageUrl;
    private Double perAmount;
    private Double amount;
    private Double expectAmount;
    private String currency;
    private Double exchangeRate;
    private Double gst;
    private Double deductGst;
    private Double bonus;
    private String remarks;
    private String closedReason;
    private Boolean isHistory;
    private String verifyCode;
    private String refNo;
    private String officialData;
    private List<ServiceOrderDO> subServiceOrders;
}
