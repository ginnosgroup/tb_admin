package org.zhinanzhen.b.service;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.service.pojo.WebLogDTO;

import java.util.List;

public interface WebLogService {
    List<WebLogDTO> listByServiceOrderId(@Param("serviceOrderId") Integer serviceOrderId,
                                         @Param("commissionOrderId") Integer commissionOrderId,
                                         @Param("visaId") Integer visaId,
                                         @Param("visaOfficialId") Integer visaOfficialId,
                                         @Param("schoolId") Integer schoolId, @Param("userId") Integer userId,
                                         @Param("isLogin") Integer isLogin, @Param("operatedUser") Integer operatedUser,
                                         @Param("offset") Integer offset, @Param("rows") Integer rows);

    Integer count(Integer serviceOrderId, Integer commissionOrderId, Integer visaId, Integer visaOfficialId,
                  Integer schoolId, Integer userId, Integer isLogin, Integer operatedUser);

    int addWebLogs (WebLogDTO webLog);

}
