package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.b.service.pojo.ReceiveTypeDTO;

@Data
public class VisaDO implements Serializable {

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

	private double performanceRatio = 1.00;

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

	/** 仅用于接口返回（服务订单详情里的 visaDOList 需要 applicant.id），不对应数据库字段。
	 *  未填充时不输出该字段，保证其它直出 VisaDO 的接口 JSON 不变。 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private ApplicantDTO applicant;

	private double rate; //文案等级

	private Date submitIbDate;//提交移民局时间

	private Double commissionAmount; //计入佣金提点金额(确认)

	private double refund;//退款

	private Double predictCommission;//预估佣金

	private Double maxPrice;// 服务定价

	private RefundDO refundDO;
}
