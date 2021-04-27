package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.MailLogDO;

public interface MailLogDAO {

	int addMailLog(MailLogDO mailLogDo);

	List<MailLogDO> listMailLog();

	MailLogDO getMailLogByCode(@Param("code") String code);

}
