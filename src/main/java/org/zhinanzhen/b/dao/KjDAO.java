package org.zhinanzhen.b.dao;

import java.util.List;
import org.zhinanzhen.b.dao.pojo.KjDO;

import org.apache.ibatis.annotations.Param;

public interface KjDAO {

	public int addKj(KjDO kjDo);

	public int updateKj(KjDO kjDo);

	public int countKj(@Param("name") String name, @Param("regionId") Integer regionId);

	public List<KjDO> listKj(@Param("name") String name, @Param("regionId") Integer regionId,
			@Param("offset") int offset, @Param("rows") int rows);

	public KjDO getKjById(int id);

}
