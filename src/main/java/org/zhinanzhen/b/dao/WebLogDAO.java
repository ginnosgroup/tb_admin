package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.service.pojo.WebLogDTO;

import java.util.List;

public interface WebLogDAO {
    int addWebLogs (WebLogDTO webLog);

    List<WebLogDTO> listWebLogs (@Param("serviceOrderId") Integer serviceOrderId,@Param("userId") Integer userId, @Param("operatedUser") Integer operatedUser, @Param("offset") Integer offset, @Param("rows") Integer rows);

    Integer count(@Param("serviceOrderId") Integer serviceOrderId,@Param("operatedUser") Integer operatedUser);

}
