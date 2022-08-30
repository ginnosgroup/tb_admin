package org.zhinanzhen.b.service.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
@ToString
public class CommissionInfoDTO  {



    @Getter
    @Setter
    private String commissionorderid;


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
