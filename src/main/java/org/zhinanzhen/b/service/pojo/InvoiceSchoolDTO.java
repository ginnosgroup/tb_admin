package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.InvoiceSchoolDO;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/26 15:21
 * Description:
 * Version: V1.0
 */
@Data
public class InvoiceSchoolDTO extends InvoiceSchoolDO {

    private BigDecimal GST;

    private BigDecimal totalGST;
}
