package org.zhinanzhen.b.dao;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.b.dao.pojo.RemindDO;

public interface RemindDAO {

	public int addRemind(RemindDO remindDo);

	public List<RemindDO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId);
	
	public List<RemindDO> listRemindByVisaId(int visaId);
	
	public List<RemindDO> listRemindByBrokerageSaId(int brokerageSaId);

	public List<RemindDO> listRemindByRemindDate(Date date);

	public int deleteRemindById(int id);
	
	public int deleteRemindBySchoolBrokerageSaId(int id);
	
	public int deleteRemindByVisaId(int id);
	
	public int deleteRemindByBrokerageSaId(int id);

}
