package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ApplicantDO;

public interface ApplicantDAO {

	int add(ApplicantDO applicantDo);

	int count(@Param("id") Integer id, @Param("name") String name, @Param("userId") Integer userId,
			@Param("adviserId") Integer adviserId);

	List<ApplicantDO> list(@Param("id") Integer id, @Param("name") String name, @Param("userId") Integer userId,
			@Param("adviserId") Integer adviserId, @Param("offset") int offset, @Param("rows") int rows);

	ApplicantDO getById(int id);

	ApplicantDO getPrimaryByUserId(@Param("userId") int userId);

	int update(ApplicantDO applicantDo);

	int updateByWechat(ApplicantDO applicantDo);

	int deleteById(int id);

	List<ApplicantDO> listByIds(@Param("ids") List<Integer> ids);

	List<ApplicantDO> listByUserIds(@Param("userIds") List<Integer> userIds, @Param("adviserId") Integer adviserId);

}
