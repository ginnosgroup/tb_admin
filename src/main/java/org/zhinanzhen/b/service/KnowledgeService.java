package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.KnowledgeDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface KnowledgeService {

	int addKnowledge(KnowledgeDTO knowledgeDto) throws ServiceException;

	int updateKnowledge(KnowledgeDTO knowledgeDto) throws ServiceException;

	int countKnowledge(Integer knowledgeMenuId, String keyword) throws ServiceException;

	List<KnowledgeDTO> listKnowledge(Integer knowledgeMenuId, String keyword, String password, int pageNum,
			int pageSize) throws ServiceException;

	KnowledgeDTO getKnowledge(Integer id, String password) throws ServiceException;

	int deleteKnowledge(int id) throws ServiceException;

}
