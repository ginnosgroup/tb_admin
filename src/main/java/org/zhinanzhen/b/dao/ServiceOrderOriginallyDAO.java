package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.service.pojo.WebLogDTO;
import org.zhinanzhen.tb.dao.pojo.ServiceOrderOriginallyDO;

import java.util.List;

public interface ServiceOrderOriginallyDAO {
    int addServiceOrderOriginallyDO(ServiceOrderOriginallyDO serviceOrderOriginallyDO);

    List<ServiceOrderOriginallyDO> listServiceOrderOriginallyDO(@Param("serviceOrderId") Integer serviceOrderId, @Param("webLogId") Integer webLogId);


}
