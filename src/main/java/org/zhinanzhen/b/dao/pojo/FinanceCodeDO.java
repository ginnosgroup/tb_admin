package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2020/12/18 13:54
 * Description:
 * Version: V1.0
 */
@Data
public class FinanceCodeDO {

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    //入账时间
    private Date bankDate;

    private int userId;

    //是否收入
    private boolean isIncome;

    //收入/支出金额
    private double money;

    //余额
    private double balance;

    private int adviserId;

    //业务
    private String business;

    //private  int commissionOrderId;

    //private int visaId;

    //本次实收金额 来自佣金订单
    private double amount;

    private String orderId;

    //银行备注
    private String comment;
}
