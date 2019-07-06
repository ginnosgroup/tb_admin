package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.MessageTopicDO;

public interface MessageTopicDAO {

	int addMessageTopic(MessageTopicDO messageTopicDo);

	List<MessageTopicDO> listMessageTopic(@Param("userId") Integer userId);

	int deleteMessageTopic(int id);

}
