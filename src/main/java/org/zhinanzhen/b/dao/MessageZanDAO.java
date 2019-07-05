package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.MessageZanDO;

public interface MessageZanDAO {

	int addMessageZan(MessageZanDO messageZanDo);

	int countMessageZan(@Param("userId") Integer userId, @Param("messageId") Integer messageId);

	int deleteMessageZanByMessageId(int id);

}
