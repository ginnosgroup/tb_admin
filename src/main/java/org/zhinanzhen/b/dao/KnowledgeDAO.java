package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface KnowledgeDAO {

	int addKnowledge(KnowledgeDO knowledgeDo);

	int updateKnowledge(KnowledgeDO knowledgeDo);

	List<SourceDO> listKnowledge(@Param("knowledgeMenuId") Integer knowledgeMenuId);

	int deleteKnowledge(int id);

}
