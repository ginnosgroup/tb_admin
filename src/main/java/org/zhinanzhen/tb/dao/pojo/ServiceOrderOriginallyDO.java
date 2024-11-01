package org.zhinanzhen.tb.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderOriginallyDO {
    private Integer id;

    private Integer serviceOrderId;

    private Integer adviserId;

    private Integer officialId;

    private Integer userId;

    private Integer newAdviserId;

    private Integer newOfficialId;

    private Integer webLogId;
}
