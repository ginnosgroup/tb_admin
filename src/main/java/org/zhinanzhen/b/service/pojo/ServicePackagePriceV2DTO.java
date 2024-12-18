package org.zhinanzhen.b.service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServicePackagePriceV2DTO {
    // 文案佣金规则（0：固定rate，1：固定金额）
    private Integer ruler;

    // 国家
    private String country;

    private Integer parentId;

    // 文案等级id集合
    private String officialGrades;

    // 固定比例
    private Double rate;

    // 城市
    private Integer areaId;

    // 固定金额
    private Double amount;
}
