package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SchoolPackageDO;

public interface ServicePackageDAO {

	public int add(SchoolPackageDO schoolPackageDo);

	public List<SchoolPackageDO> list(@Param("type") String type, @Param("subject") Integer serviceId,
			@Param("num") Integer num);

	public SchoolPackageDO getById(int id);

}
