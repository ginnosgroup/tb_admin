package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/08/30 下午 5:55
 * Description:
 * Version: V1.0
 */
@Data
public class SchoolSettingNewDO implements Serializable {

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private int type;

    private Date startDate;

    private Date endDate;

    private String parameters;

    private int level;

    private boolean firstRegister;

    private BigDecimal registerFee;

    private BigDecimal bookFee;

    private boolean firstBook;

    private int providerId;

    private String courseLevel;

    private Integer courseId;
}
