package org.zhinanzhen.b.service;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.b.service.pojo.RemindDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface RemindService {
	
	public int addRemind(RemindDTO remindDto) throws ServiceException;

	public List<RemindDTO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId) throws ServiceException;
	
	public List<RemindDTO> listRemindByRemindDate(Date date) throws ServiceException;

	public int deleteRemindById(int id) throws ServiceException;
	
	public int deleteRemindBySchoolBrokerageSaId(int id) throws ServiceException;

}
