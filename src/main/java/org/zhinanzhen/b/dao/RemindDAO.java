package org.zhinanzhen.b.dao;

import java.util.List;

import org.zhinanzhen.b.dao.pojo.RemindDO;

public interface RemindDAO {

	public int addRemind(RemindDO remindDO);

	public List<RemindDO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId);

	public int deleteSchoolById(int id);

}
