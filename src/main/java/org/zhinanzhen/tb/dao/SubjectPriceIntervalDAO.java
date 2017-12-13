package org.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.SubjectPriceIntervalDO;

public interface SubjectPriceIntervalDAO {

	public int addSubjectPriceInterval(SubjectPriceIntervalDO subjectPriceIntervalDo);

	public List<SubjectPriceIntervalDO> listSubjectPriceInterval(@Param("subjectId") int subjectId);
	
	public boolean deleteBySubjectId(@Param("subjectId") int subjectId);

}
