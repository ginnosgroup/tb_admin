package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ApplicantDO;

public interface ApplicantDAO {

	int add(ApplicantDO applicantDo);

	int count(@Param("id") Integer id, @Param("name") String name, @Param("userId") int userId,
			@Param("adviserId") int adviserId);

	List<ApplicantDO> list(@Param("id") Integer id, @Param("name") String name, @Param("userId") int userId,
			@Param("adviserId") int adviserId, @Param("offset") int offset, @Param("rows") int rows);

	ApplicantDO getById(int id);

	int update(ApplicantDO applicantDo);

	int deleteById(int id);

}
