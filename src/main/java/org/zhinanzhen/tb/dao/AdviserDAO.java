package org.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;

public interface AdviserDAO {

	public int addAdviser(AdviserDO adviserDo);

	public int updateAdviser(AdviserDO adviserDo);

	public int countAdviser(@Param("name") String name, @Param("regionId") Integer regionId);

	public List<AdviserDO> listAdviser(@Param("name") String name, @Param("regionId") Integer regionId,
			@Param("offset") int offset, @Param("rows") int rows);

	public AdviserDO getAdviserById(int id);

}
