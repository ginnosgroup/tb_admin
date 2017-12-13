package org.zhinanzhen.tb.dao.pojo;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class PayLogDO {

    private Integer id;

    private Integer userId;

    private Integer orderId;

    private String payType;

    private String payCode;

    private BigDecimal payAmount;

    private Date payDate;

}