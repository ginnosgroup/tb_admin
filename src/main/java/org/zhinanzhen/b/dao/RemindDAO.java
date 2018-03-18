package org.zhinanzhen.b.dao;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.b.dao.pojo.RemindDO;

public interface RemindDAO {

	public int addRemind(RemindDO remindD);

	public List<RemindDO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId);

	public List<RemindDO> listRemindByRemindDate(Date date);

	public int deleteRemindById(int id);
	
	public int deleteRemindBySchoolBrokerageSaId(int id);

}
