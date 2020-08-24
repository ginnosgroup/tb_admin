package org.zhinanzhen.b.service;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.b.service.pojo.RemindDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface RemindService {

	public int addRemind(RemindDTO remindDto) throws ServiceException;

	public List<RemindDTO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId, int adviserId, AbleStateEnum state)
			throws ServiceException;

	public List<RemindDTO> listRemindByRemindDate(Date date, int adviserId) throws ServiceException;

	public int updateStateById(int id, AbleStateEnum state) throws ServiceException;

	public int deleteRemindById(int id) throws ServiceException;

	public int deleteRemindBySchoolBrokerageSaId(int id) throws ServiceException;

}
