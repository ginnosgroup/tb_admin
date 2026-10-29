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

	/** 申请人ID：取自所属服务订单 b_service_order.applicant_id。
	 *  b_visa 表本身没有该列，服务层在组装签证行时统一填充；少数 SQL
	 *  （如 BServiceOrderDAO.getCommissionOrderList）已 SELECT so.applicant_id AS applicantId，可直接映射进来。
	 *  用基本类型是为了保证该字段一定会出现在 JSON 里（0 表示未关联申请人），
	 *  前端按签证行归并/展示申请人时直接读它，不再依赖 applicant 对象是否存在。 */
	private int applicantId;

	/** 仅用于接口返回（服务订单详情里的 visaDOList 需要 applicant.id），不对应数据库字段。
	 *  未填充时不输出该字段，保证其它直出 VisaDO 的接口 JSON 不变。
	 *  注意：新代码请优先用上面的 applicantId，本字段仅作旧前端展示兼容。 */
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
