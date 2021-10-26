package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.CommissionOrderTempDO;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/10/21 下午 3:06
 * Description:
 * Version: V1.0
 */
@Data
public class CommissionOrderTempDTO {

    private int id;

    private Date gmtCreate;

    private int installment;

    private Date installmentDueDate1;

    private Date installmentDueDate2;

    private Date installmentDueDate3;

    private Date installmentDueDate4;

    private Date installmentDueDate5;

    private Date installmentDueDate6;

    private Date installmentDueDate7;

    private Date installmentDueDate8;

    private Date installmentDueDate9;

    private Date installmentDueDate10;

    private Date installmentDueDate11;

    private Date installmentDueDate12;

    private String paymentVoucherImageUrl1;

    private String paymentVoucherImageUrl2;

    private String paymentVoucherImageUrl3;

    private String paymentVoucherImageUrl4;

    private String paymentVoucherImageUrl5;

    private int serviceOrderId;

    private String studentCode;

    private Date dob;

    private Date startDate;

    private Date endDate;

    private double tuitionFee;//总学费

    private double perTermTuitionFee;//每学期学费

    private int receiveTypeId;

    private ReceiveTypeDTO receiveType;

    private Date receiveDate;//本次收款日期

    private double perAmount;//本次应收

    private double amount;//本次实收

    private double discount;//折扣

    private String verifyCode;//对账code

    private double expectAmount;//预收业绩

    private String remarks;

}
