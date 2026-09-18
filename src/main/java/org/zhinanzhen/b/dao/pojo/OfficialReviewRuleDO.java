package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 文案接单规则。
 * serviceId=0 表示该文案的默认规则，具体服务规则优先级更高。
 */
@Data
public class OfficialReviewRuleDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private int officialId;

    private int serviceId;

    private Integer canAccept;

    private Double minReceivable;

    private int isDelete;
}
