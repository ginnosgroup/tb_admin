package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/19 16:46
 * Description:
 * Version: V1.0
 */
@Data
public class InvoiceServiceFeeDescriptionDO {

    private int id;

    private String description;

    private BigDecimal unitPrice;

    private int quantity;

    private BigDecimal amount;
}
