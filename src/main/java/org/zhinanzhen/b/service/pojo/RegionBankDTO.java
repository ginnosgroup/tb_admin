package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.FinanceBankDO;

/**
 * Created with IntelliJ IDEA.
 * Date: 2020/12/25 17:10
 * Description:
 * Version: V1.0
 */
@Data
public class RegionBankDTO {
    private int id;

    private String name;

    private Integer parentId;

    private int weight;

    private int financeBankId;

    private FinanceBankDO financeBankDO;
}
