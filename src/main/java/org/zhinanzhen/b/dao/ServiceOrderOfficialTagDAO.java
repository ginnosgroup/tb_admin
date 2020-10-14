package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceOrderOfficialTagDO;

public interface ServiceOrderOfficialTagDAO {

	int addServiceOrderOfficialTag(ServiceOrderOfficialTagDO serviceOrderOfficialTagDo);

	int deleteServiceOrderOfficialTagByTagIdAndServiceOrderId(@Param("officialTagId") int officialTagId,
			@Param("serviceOrderId") int serviceOrderId);

	int deleteServiceOrderOfficialTagByOfficialTagId(int officialTagId);

}
