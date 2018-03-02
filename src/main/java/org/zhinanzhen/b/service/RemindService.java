package org.zhinanzhen.b.service;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.b.service.pojo.RemindDTO;

public interface RemindService {
	
	public int addRemind(RemindDTO remindDto);

	public List<RemindDTO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId);
	
	public List<RemindDTO> listRemindByRemindDate(Date date);

	public int deleteSchoolById(int id);

}
