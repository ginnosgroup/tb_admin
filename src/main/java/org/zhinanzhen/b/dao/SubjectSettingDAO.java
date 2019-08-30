package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SubjectSettingDO;

public interface SubjectSettingDAO {

	public int add(SubjectSettingDO subjectSettingDo);

	public int update(@Param("id") int id, @Param("price") double price);

	public List<SubjectSettingDO> list(@Param("schoolSettingId") int schoolSettingId);

	public SubjectSettingDO get(@Param("name") String name);

}
