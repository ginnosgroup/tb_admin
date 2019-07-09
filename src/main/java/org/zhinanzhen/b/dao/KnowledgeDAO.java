package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.KnowledgeDO;

public interface KnowledgeDAO {

	int addKnowledge(KnowledgeDO knowledgeDo);

	int updateKnowledge(KnowledgeDO knowledgeDo);

	List<KnowledgeDO> listKnowledge(@Param("knowledgeMenuId") Integer knowledgeMenuId,
			@Param("keyword") String keyword);

	KnowledgeDO getKnowledge(@Param("id") int id);

	int deleteKnowledge(int id);

}
