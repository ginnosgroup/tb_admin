package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.MessageDTO;
import org.zhinanzhen.b.service.pojo.MessageListDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface MessageService {

	int addMessage(MessageDTO messageDto) throws ServiceException;

	int zan(Integer adminUserId, Integer messageId) throws ServiceException;

	List<MessageListDTO> listMessage(Integer knowledgeId) throws ServiceException;

	int deleteMessage(int id) throws ServiceException;

}
