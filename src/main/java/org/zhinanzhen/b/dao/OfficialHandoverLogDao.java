package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;

public interface OfficialHandoverLogDao {

    void add(@Param("serviceOrderId") Integer serviceOrderId,
             @Param("newOfficialId") Integer newOfficialId,
             @Param("officialId") Integer officialId
             );

}
