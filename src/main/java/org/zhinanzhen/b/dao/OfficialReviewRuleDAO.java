package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.OfficialReviewRuleDO;

public interface OfficialReviewRuleDAO {

    /**
     * 查询文案针对指定服务的规则。具体服务规则优先于service_id=0的默认规则。
     */
    OfficialReviewRuleDO getByOfficialIdAndServiceId(@Param("officialId") int officialId,
                                                      @Param("serviceId") int serviceId);
}
