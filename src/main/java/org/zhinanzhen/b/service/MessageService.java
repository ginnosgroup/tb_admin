package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.MessageDTO;
import org.zhinanzhen.b.service.pojo.MessageListDTO;
import org.zhinanzhen.b.service.pojo.MessageTopicDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface MessageService {

	int addMessageTopic(MessageTopicDTO messageTopicDto) throws ServiceException;

	int addMessage(MessageDTO messageDto) throws ServiceException;

	public List<MessageTopicDTO> listMessageTopic(Integer userId) throws ServiceException;

	public List<MessageListDTO> listMessage(Integer topicId) throws ServiceException;

	public int deleteMessageTopic(int id) throws ServiceException;

	public int deleteMessage(int id) throws ServiceException;

}
