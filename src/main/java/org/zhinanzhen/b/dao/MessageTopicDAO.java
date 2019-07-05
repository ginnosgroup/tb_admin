package org.zhinanzhen.b.dao;

import java.util.List;

import org.zhinanzhen.b.dao.pojo.MessageTopicDO;

public interface MessageTopicDAO {

	int addMessageTopic(MessageTopicDO messageTopicDo);

	List<MessageTopicDO> listMessageTopic(int userId);

	int deleteMessageTopic(int id);

}
