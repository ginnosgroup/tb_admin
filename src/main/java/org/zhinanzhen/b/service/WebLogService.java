package org.zhinanzhen.b.service;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.service.pojo.WebLogDTO;

import java.util.List;

public interface WebLogService {
    List<WebLogDTO> listByServiceOrderId(@Param("serviceOrderId") Integer serviceOrderId, @Param("userId") Integer userId, @Param("userId") Integer operatedUser, @Param("offset") Integer offset, @Param("rows") Integer rows);

    Integer count(Integer serviceOrderId, Integer operatedUser);

    int addWebLogs (WebLogDTO webLog);

}
