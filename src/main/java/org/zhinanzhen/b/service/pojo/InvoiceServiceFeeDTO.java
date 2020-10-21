package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.InvoiceServiceFeeDO;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/21 13:39
 * Description:
 * Version: V1.0
 */
@Data
public class InvoiceServiceFeeDTO extends InvoiceServiceFeeDO {

    private BigDecimal subtotal;

    private BigDecimal GST;

    private BigDecimal totalGST;
}
