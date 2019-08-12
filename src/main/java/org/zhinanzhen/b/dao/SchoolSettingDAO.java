package org.zhinanzhen.b.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SchoolSettingDO;

public interface SchoolSettingDAO {

	public int add(SchoolSettingDO schoolSettingDo);

	public int update(@Param("id") int id, @Param("type") int type, @Param("startDate") Date startDate,
			@Param("endDate") Date endDate, @Param("parameters") String parameters);

	public List<SchoolSettingDO> list();

	public SchoolSettingDO get(@Param("name") String name);

}
