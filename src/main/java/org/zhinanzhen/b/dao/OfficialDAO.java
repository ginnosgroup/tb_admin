package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.OfficialDO;

public interface OfficialDAO {

	public int addOfficial(OfficialDO officialDo);

	public int updateOfficial(OfficialDO adviserDo);

	public int countOfficial(@Param("name") String name, @Param("regionId") Integer regionId);

	public List<OfficialDO> listOfficial(@Param("name") String name, @Param("regionId") Integer regionId,
			@Param("offset") int offset, @Param("rows") int rows);

	public OfficialDO getOfficialById(int id);

	OfficialDO getOfficialByGradeId(int gradeId);
	Integer getOfficialAdmin (int id);
}
