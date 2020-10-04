package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;

public interface ServiceOrderOfficialTagDAO {

	int addServiceOrderOfficialTag(@Param("officialTagId") int officialTagId, @Param("serviceOrderId") int serviceOrderId);

	int deleteServiceOrderOfficialTagById(int id);
	
	int deleteServiceOrderOfficialTagByOfficialTagId(int officialTagId);

}
