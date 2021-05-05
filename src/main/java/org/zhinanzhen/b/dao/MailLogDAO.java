package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.MailLogDO;

public interface MailLogDAO {

	int addMailLog(MailLogDO mailLogDo);

	public int countMailLog();

	List<MailLogDO> listMailLog(@Param("offset") int offset, @Param("rows") int rows);

	MailLogDO getMailLogByCode(@Param("code") String code);

	void refresh(@Param("id") int id);

}
