package org.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.SubjectDO;
import org.zhinanzhen.tb.dao.pojo.SubjectUpdateDO;

public interface SubjectDAO {

	public int addSubject(SubjectDO subjectDo);

	public int updateSubjectState(@Param("id") int id, @Param("state") String state);

	public int updateSubject(SubjectUpdateDO subjectUpdateDO);
	
	public int updateSubjectWeight(@Param("id") int id, @Param("weight") int weight);

	public int updateSubjectWeightPlus(@Param("weight") int weight);
	
	public int updateSubjectCategory(@Param("id") int id,@Param("categoryId") int categoryId);

	public int countSubject(@Param("keyword") String keyword, @Param("categoryId") Integer categoryId,
			@Param("state") String state);

	public List<SubjectDO> listSubject(@Param("keyword") String keyword, @Param("categoryId") Integer categoryId,
			@Param("state") String state, @Param("offset") int offset, @Param("rows") int rows);
	
	public List<SubjectDO> listSubjectByParentId(int id);

	public SubjectDO getSubjectById(int id);
	
	public SubjectDO getFirstSubject();

	public int deleteSubjectById(int id);
}