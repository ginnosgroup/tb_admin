package org.zhinanzhen.b.dao.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


public class CommissionInfoDO extends CommissionOrderDO  {
    private static final long serialVersionUID = 1L;


    @Getter
    @Setter
    private Date gmtCreate;

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private int serviceOrderId;

    @Getter
    @Setter
    private String state;

    @Getter
    @Setter
    private String studentCode;

    @Getter
    @Setter
    private int installmentNum;

    @Getter
    @Setter
    private int installment;

    @Getter
    @Setter
    private Date installmentDueDate;

    @Getter
    @Setter
    private Date startDate;

    @Getter
    @Setter
    private Date endDate;

    @Getter
    @Setter
    private double tuitionFee;

    @Getter
    @Setter
    private double perTermTuitionFee;
}
