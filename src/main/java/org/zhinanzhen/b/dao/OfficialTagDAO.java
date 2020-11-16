package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.OfficialTagDO;

public interface OfficialTagDAO {

	int addOfficialTag(OfficialTagDO officialTagDO);
	
	int updateOfficialTag(OfficialTagDO officialTagDO);

	List<OfficialTagDO> listOfficialTag();

	List<OfficialTagDO> listOfficialTagByServiceOrderId(@Param("serviceOrderId") Integer serviceOrderId);

	OfficialTagDO getOfficialTagById(@Param("id") Integer id);
	
	OfficialTagDO getOfficialTagByServiceOrderId(@Param("serviceOrderId") Integer serviceOrderId);

	int deleteOfficialTagById(@Param("id") Integer id);

}
