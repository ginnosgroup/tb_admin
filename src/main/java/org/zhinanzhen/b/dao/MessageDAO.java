package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.MessageDO;

public interface MessageDAO {

	int addMessage(MessageDO messageDo);

	public List<MessageDO> listMessage(@Param("knowledgeId") Integer knowledgeId);

	public int deleteMessage(int id);

}
