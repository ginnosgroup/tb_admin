package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OfficialHandoverLogDao {

    void add(@Param("serviceOrderId") Integer serviceOrderId,
             @Param("officialId") Integer officialId,
             @Param("newOfficialId") Integer newOfficialId
             );
    Integer getOldOfficial(@Param("serviceOrderId") Integer serviceOrderId);

    List<Map<String, Object>> listOldOfficialsByServiceOrderIds(@Param("serviceOrderIds") List<Integer> serviceOrderIds);
}
