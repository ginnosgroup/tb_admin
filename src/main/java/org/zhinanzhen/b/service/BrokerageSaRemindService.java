package org.zhinanzhen.b.service;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.b.service.pojo.BrokerageSaRemindDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface BrokerageSaRemindService {

	public int addRemind(BrokerageSaRemindDTO brokerageSaRemindDTO) throws ServiceException;

	public List<BrokerageSaRemindDTO> listRemindByBrokerageSaId(int visaId, int adviserId, AbleStateEnum state)
			throws ServiceException;

	public List<BrokerageSaRemindDTO> listRemindByRemindDate(Date date, int adviserId) throws ServiceException;

	public int updateStateByBrokerageSaId(int id, AbleStateEnum state) throws ServiceException;

	public int deleteRemindByBrokerageSaId(int id) throws ServiceException;

}
