package org.zhinanzhen.b.dao;

import java.util.List;
import org.zhinanzhen.b.dao.pojo.MaraDO;

import org.apache.ibatis.annotations.Param;

public interface MaraDAO {

	public int addMara(MaraDO maraDo);

	public int updateMara(MaraDO maraDo);

	public int countMara(@Param("name") String name, @Param("regionId") Integer regionId);

	public List<MaraDO> listMara(@Param("name") String name, @Param("regionId") Integer regionId,
			@Param("offset") int offset, @Param("rows") int rows);

	public MaraDO getMaraById(int id);

}
