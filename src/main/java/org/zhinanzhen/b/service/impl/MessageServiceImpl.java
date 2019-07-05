package org.zhinanzhen.b.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.service.MessageService;
import org.zhinanzhen.b.service.pojo.MessageDTO;
import org.zhinanzhen.b.service.pojo.MessageListDTO;
import org.zhinanzhen.b.service.pojo.MessageTopicDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

@Service("MessageService")
public class MessageServiceImpl extends BaseService implements MessageService {

	@Override
	public int addMessageTopic(MessageTopicDTO messageTopicDto) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addMessage(MessageDTO messageDto) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<MessageTopicDTO> listMessageTopic(Integer userId) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MessageListDTO> listMessage(Integer topicId) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int deleteMessageTopic(int id) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteMessage(int id) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

}
