package org.zhinanzhen.b.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.RemindDO;

public interface RemindDAO {

	public int addRemind(RemindDO remindDo);

	public List<RemindDO> listRemindByServiceOrderId(@Param("serviceOrderId") int serviceOrderId,
			@Param("adviserId") Integer adviserId, @Param("state") String state);

	public List<RemindDO> listRemindByRemindDate(Date date);

	public int updateStateById(@Param("id") int id, @Param("state") String state);

	@Deprecated
	public int deleteRemindById(int id);

	public int deleteRemindByServiceOrderId(int id);

}
