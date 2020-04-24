package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.MessageDAO;
import org.zhinanzhen.b.dao.MessageZanDAO;
import org.zhinanzhen.b.dao.pojo.MessageDO;
import org.zhinanzhen.b.dao.pojo.MessageZanDO;
import org.zhinanzhen.b.service.MessageService;
import org.zhinanzhen.b.service.pojo.MessageDTO;
import org.zhinanzhen.b.service.pojo.MessageListDTO;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("MessageService")
public class MessageServiceImpl extends BaseService implements MessageService {

	@Resource
	private MessageDAO messageDao;

	@Resource
	private MessageZanDAO messageZanDao;

	@Resource
	private AdminUserDAO adminUserDao;

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
	public int zan(Integer adminUserId, Integer messageId) throws ServiceException {
		if (adminUserId <= 0) {
			ServiceException se = new ServiceException("adminUserId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (messageId <= 0) {
			ServiceException se = new ServiceException("messageId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (messageZanDao.countMessageZan(adminUserId, messageId) > 0) {
			ServiceException se = new ServiceException("You can't Zan !");
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		MessageZanDO messageZanDo = new MessageZanDO();
		messageZanDo.setAdminUserId(adminUserId);
		messageZanDo.setMessageId(messageId);
		return messageZanDao.addMessageZan(messageZanDo);
	}

	@Override
	public List<MessageListDTO> listMessage(Integer knowledgeId) throws ServiceException {
		List<MessageListDTO> messageListDtoList = new ArrayList<>();
		List<MessageDO> messageDoList = new ArrayList<>();
		try {
			messageDoList = messageDao.listMessage(knowledgeId);
			if (messageDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (MessageDO messageDo : messageDoList) {
			MessageListDTO messageListDto = mapper.map(messageDo, MessageListDTO.class);
			AdminUserDO adminUserDo = adminUserDao.getAdminUserById(messageDo.getAdminUserId());
			if (adminUserDo != null)
				messageListDto.setAdminUserName(adminUserDo.getUsername());
			messageListDto.setZan(messageZanDao.countMessageZan(messageDo.getAdminUserId(), messageDo.getId()));
			messageListDtoList.add(messageListDto);
		}
		return messageListDtoList;
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
