package org.zhinanzhen.b.service.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/08/31 下午 4:20
 * Description:
 * Version: V1.0
 */
@Data
public class SchoolSettingNewDTO {
    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private int type;

    private Date startDate;

    private Date endDate;

    private String parameters;

    private int level;

    private boolean firstRegister;

    private double registerFee;

    private double bookFee;

    private boolean firstBook;

    private int providerId;

    private String courseLevel;

    private Integer courseId;
}
