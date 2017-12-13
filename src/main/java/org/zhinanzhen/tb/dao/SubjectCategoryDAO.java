package org.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.SubjectCategoryDO;

public interface SubjectCategoryDAO {

	public int addSubjectCategory(SubjectCategoryDO subjectCategoryDo);

	public int updateSubjectCategoryState(@Param("id") int id, @Param("state") String state);

	public int updateSubjectCategory(SubjectCategoryDO subjectCategoryDo);

	public int countSubjectCategory(@Param("state") String state);

	public List<SubjectCategoryDO> listSubjectCategory(@Param("state") String state, @Param("offset") int offset,
			@Param("rows") int rows);

	public SubjectCategoryDO getSubjectCategoryById(int id);

}
