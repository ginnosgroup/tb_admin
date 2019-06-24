package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.KnowledgeMenuDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface KnowledgeMenuService {

	int addKnowledgeMenu(KnowledgeMenuDTO knowledgeMenuDto) throws ServiceException;

	int updateKnowledgeMenu(KnowledgeMenuDTO knowledgeMenuDto) throws ServiceException;

	List<KnowledgeMenuDTO> listKnowledgeMenu(Integer parentId) throws ServiceException;

	int deleteKnowledgeMenu(int id) throws ServiceException;

}
