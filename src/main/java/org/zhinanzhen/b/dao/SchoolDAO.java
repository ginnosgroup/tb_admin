package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SchoolDO;

public interface SchoolDAO {

	public int addSchool(SchoolDO schoolDO);

	public int updateSchool(@Param("id") int id, @Param("name") String name, @Param("subject") String subject,
			@Param("country") String country);

	public List<SchoolDO> listSchool(@Param("name") String name, @Param("subject") String subject,
			@Param("country") String country);

	public SchoolDO getSchoolById(int id);

	public int deleteSchoolById(int id);

}
