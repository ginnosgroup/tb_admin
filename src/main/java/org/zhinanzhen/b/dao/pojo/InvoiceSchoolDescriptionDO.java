package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/20 10:16
 * Description:
 * Version: V1.0
 */
@Data
public class InvoiceSchoolDescriptionDO {

    private int id;

    private String  studentname;

    private Date  dob;

    private int studentId;

    private String course;

    private Date startDate;

    private String instalMent;

    //学费
    private double tuitionFee;

    //奖金
    private double bonus;

    //佣金率
    private double commissionrate;

    //佣金
    private double commission;
}
