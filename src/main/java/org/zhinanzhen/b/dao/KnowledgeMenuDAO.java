package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.KnowledgeMenuDO;

public interface KnowledgeMenuDAO {

	public int addKnowledgeMenu(KnowledgeMenuDO knowledgeMenuDo);

	int updateKnowledgeMenu(KnowledgeMenuDO knowledgeMenuDo);

	List<KnowledgeMenuDO> listKnowledgeMenu(@Param("parentId") Integer parentId);

	KnowledgeMenuDO getKnowledgeMenu(@Param("id") int id);

	int deleteKnowledgeMenu(int id);
}
