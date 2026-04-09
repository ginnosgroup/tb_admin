package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.OfficialEvaluate;

public interface OfficialDAO {

	public int addOfficial(OfficialDO officialDo);

	public int updateOfficial(OfficialDO adviserDo);

	public int countOfficial(@Param("name") String name, @Param("regionId") Integer regionId, @Param("gradeId") Integer gradeId);

	public List<OfficialDO> listOfficial(@Param("name") String name, @Param("regionId") Integer regionId, @Param("gradeId") Integer gradeId,
			@Param("offset") int offset, @Param("rows") int rows);

	public OfficialDO getOfficialById(int id);

	List<OfficialDO> getOfficialByGradeId(int gradeId);
	Integer getOfficialAdmin (int id);

	@Select("select * from b_official where name = #{creatorName} limit 0,1")
	List<OfficialDO> getOfficialByName(String creatorName);

    int addOfficialEvaluate(OfficialEvaluate officialEvaluate);

	List<OfficialEvaluate> listOfficialEvaluate(@Param("officialIds")List<Integer> officialIds, @Param("adviserId")Integer adviserId,
												@Param("startCollaborationTime")String startCollaborationTime, @Param("endCollaborationTime")String endCollaborationTime,
												@Param("pageNum")Integer pageNum, @Param("pageSize")Integer pageSize);

	int countOfficialEvaluate(@Param("officialIds")List<Integer> officialIds, @Param("adviserId")Integer adviserId, @Param("startCollaborationTime")String startCollaborationTime, @Param("endCollaborationTime")String endCollaborationTime);

	int updateOfficialEvaluate(OfficialEvaluate officialEvaluate);

	OfficialEvaluate getOfficialEvaluate(@Param("officialId") Integer officialId, @Param("adviserId")Integer adviserId, @Param("startCollaborationTime")String startCollaborationTime, @Param("endCollaborationTime")String endCollaborationTime, @Param("id") Integer id);

	OfficialDO getOfficialByEmail(@Param("userName") String userName);

	List<OfficialDO> listByIds(@Param("ids") List<Integer> ids);

}
