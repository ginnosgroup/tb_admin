package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.KnowledgeDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface KnowledgeService {

	int addKnowledge(KnowledgeDTO knowledgeDto) throws ServiceException;

	int updateKnowledge(KnowledgeDTO knowledgeDto) throws ServiceException;

	List<KnowledgeDTO> listKnowledge(Integer knowledgeMenuId, String keyword) throws ServiceException;

	KnowledgeDTO getKnowledge(Integer id) throws ServiceException;

	int deleteKnowledge(int id) throws ServiceException;

}
