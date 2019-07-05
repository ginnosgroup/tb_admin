package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.MessageDAO;
import org.zhinanzhen.b.dao.MessageTopicDAO;
import org.zhinanzhen.b.dao.MessageZanDAO;
import org.zhinanzhen.b.dao.pojo.MessageDO;
import org.zhinanzhen.b.dao.pojo.MessageTopicDO;
import org.zhinanzhen.b.dao.pojo.MessageZanDO;
import org.zhinanzhen.b.service.MessageService;
import org.zhinanzhen.b.service.pojo.MessageDTO;
import org.zhinanzhen.b.service.pojo.MessageListDTO;
import org.zhinanzhen.b.service.pojo.MessageTopicDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("MessageService")
public class MessageServiceImpl extends BaseService implements MessageService {

	@Resource
	private MessageTopicDAO messageTopicDao;

	@Resource
	private MessageDAO messageDao;

	@Resource
	private MessageZanDAO messageZanDao;

	@Override
	public int addMessageTopic(MessageTopicDTO messageTopicDto) throws ServiceException {
		if (messageTopicDto == null) {
			ServiceException se = new ServiceException("messageTopicDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			MessageTopicDO messageTopicDo = mapper.map(messageTopicDto, MessageTopicDO.class);
			if (messageTopicDao.addMessageTopic(messageTopicDo) > 0) {
				messageTopicDto.setId(messageTopicDo.getId());
				return messageTopicDo.getId();
			} else {
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int addMessage(MessageDTO messageDto) throws ServiceException {
		if (messageDto == null) {
			ServiceException se = new ServiceException("messageDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			MessageDO messageDo = mapper.map(messageDto, MessageDO.class);
			if (messageDao.addMessage(messageDo) > 0) {
				messageDto.setId(messageDo.getId());
				return messageDo.getId();
			} else {
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int zan(Integer userId, Integer messageId) throws ServiceException {
		if (userId <= 0) {
			ServiceException se = new ServiceException("userId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (messageId <= 0) {
			ServiceException se = new ServiceException("messageId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		MessageZanDO messageZanDo = new MessageZanDO();
		messageZanDo.setUserId(userId);
		messageZanDo.setMessageId(messageId);
		return messageZanDao.addMessageZan(messageZanDo);
	}

	@Override
	public List<MessageTopicDTO> listMessageTopic(Integer userId) throws ServiceException {
		List<MessageTopicDTO> messageTopicDtoList = new ArrayList<>();
		List<MessageTopicDO> messageTopicDoList = new ArrayList<>();
		try {
			messageTopicDoList = messageTopicDao.listMessageTopic(userId);
			if (messageTopicDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (MessageTopicDO messageTopicDo : messageTopicDoList) {
			MessageTopicDTO messageTopicDto = mapper.map(messageTopicDo, MessageTopicDTO.class);
			messageTopicDtoList.add(messageTopicDto);
		}
		return messageTopicDtoList;
	}

	@Override
	public List<MessageListDTO> listMessage(Integer topicId) throws ServiceException {
		List<MessageListDTO> messageListDtoList = new ArrayList<>();
		List<MessageDO> messageDoList = new ArrayList<>();
		try {
			messageDoList = messageDao.listMessage(topicId);
			if (messageDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (MessageDO messageDo : messageDoList) {
			MessageListDTO messageListDto = mapper.map(messageDo, MessageListDTO.class);
			messageListDto.setZan(messageZanDao.countMessageZan(messageDo.getUserId(), messageDo.getId()));
			messageListDtoList.add(messageListDto);
		}
		return messageListDtoList;
	}

	@Override
	public int deleteMessageTopic(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			messageDao.listMessage(id).forEach(message -> {
				try {
					deleteMessage(message.getId());
				} catch (ServiceException e) {
				}
			});
			return messageTopicDao.deleteMessageTopic(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteMessage(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			messageZanDao.deleteMessageZanByMessageId(id);
			return messageDao.deleteMessage(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
