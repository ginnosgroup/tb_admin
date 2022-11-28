package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/12/07 9:16
 * Description:
 * Version: V1.0
 */
@Data
public class EachRegionNumberDO {

    private  int count;

    /**
     * 顾问地区name
     */
    private String name;

    /**
     * OVST:学校名字  VISA:服务code
     */
    private String code;

    /**
     * OVST:旧学校库存学校id,新学校库存课程id  VISA:b_service.id
     */
    private String serviceId;
    
    private Integer servicePackageId;

    /**
     * 下面这三个Name意思是新学校库学校name
     */
    private String instiName;

    private String institutionTradingName;

    private String institutionName;
}
