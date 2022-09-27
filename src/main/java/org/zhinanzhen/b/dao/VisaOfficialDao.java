package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.VisaOfficialDO;

public interface VisaOfficialDao {
    int countVisaByServiceOrderIdAndExcludeCode(@Param("serviceOrderId") Integer serviceOrderId,
                                                @Param("code") String code);

    int addVisa(VisaOfficialDO visaOfficialDO);
}
