package org.zhinanzhen.b.service;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.b.service.pojo.VisaRemindDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface VisaRemindService {

	public int addRemind(VisaRemindDTO visaRemindDto) throws ServiceException;

	public List<VisaRemindDTO> listRemindByVisaId(int visaId) throws ServiceException;

	public List<VisaRemindDTO> listRemindByRemindDate(Date date) throws ServiceException;

	public int deleteRemindByVisaId(int id) throws ServiceException;

}
