package org.zhinanzhen.b.dao.pojo;

import lombok.Data;
import org.zhinanzhen.b.service.pojo.ReceiveTypeDTO;

import java.util.Date;

/**
 * @Author: HL
 * @CreateTime: 2022-09-27
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class VisaOfficialDO {
    private static final long serialVersionUID = 1L;

    private int id;

    private Date gmtCreate;

    private String code;

    private Date handlingDate;

    private int userId;

    private String state;

    private String commissionState;

    private Date kjApprovalDate;

    private int receiveTypeId;

    private Date receiveDate;

    private int serviceId;

    private int serviceOrderId;

    private int installmentNum;

    private int installment;

    private String paymentVoucherImageUrl1;

    private String paymentVoucherImageUrl2;

    private String paymentVoucherImageUrl3;

    private String paymentVoucherImageUrl4;

    private String paymentVoucherImageUrl5;

    private String visaVoucherImageUrl;

    private double receivable;

    private double received;

    private double perAmount;

    private double amount;

    private double expectAmount;

    private double sureExpectAmount;

    private String currency = "AUD";

    private double exchangeRate = 4.80;

    private double discount;

    private String invoiceNumber;

    private double gst;

    private double deductGst;

    private double bonus;

    private Date bonusDate;

    private int adviserId;

    private int maraId;

    private int officialId;

    private String bankCheck;

    private boolean isChecked;

    private String remarks;

    private String refuseReason;

    private  String verifyCode;

    private Date bankDate;

    private boolean isClose;

    private Date invoiceCreate;

    private ReceiveTypeDTO receiveTypeDTO;

    private double rate; //文案等级

    private Date submitIbDate;//提交移民局时间

    private Double commissionAmount; //计入佣金提点金额(确认)

    private double refund;//退款

    private Double predictCommission;//预估佣金

    private String calculation;//计算规则

    private double predictCommissionAmount;//计入佣金提点金额(预估)

}
