package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.RemindDTO;

public interface RemindService {
	
	public int addRemind(RemindDTO remindDto);

	public List<RemindDTO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId);

	public int deleteSchoolById(int id);

}
