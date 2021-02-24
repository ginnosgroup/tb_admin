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

    private String studentId;

    private String course;

    private Date startDate;

    private String instalMent;

    //学费
    private BigDecimal tuitionFee;

    //奖金
    private BigDecimal bonus;

    //佣金率
    private String commissionrate;

    //佣金
    private BigDecimal commission;

    //专属市场bonus
    private BigDecimal marketing;
}
