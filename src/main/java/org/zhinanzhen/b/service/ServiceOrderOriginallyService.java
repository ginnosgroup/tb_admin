package org.zhinanzhen.b.service;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.ServiceOrderOriginallyDO;

import java.util.List;

public interface ServiceOrderOriginallyService {
    int addServiceOrderOriginallyDO(ServiceOrderOriginallyDO serviceOrderOriginallyDO);

    List<ServiceOrderOriginallyDO> listServiceOrderOriginallyDO(@Param("serviceOrderId") Integer serviceOrderId, @Param("webLogId") Integer webLogId,
    @Param("userId") Integer userId);
}
