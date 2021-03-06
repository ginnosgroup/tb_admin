package org.zhinanzhen.tb.service;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.tb.service.pojo.ConsultationDTO;

public interface ConsultationService {

	public int addConsultation(ConsultationDTO consultationDto) throws ServiceException;

	public int updateConsultation(ConsultationDTO consultationDto) throws ServiceException;

	public List<ConsultationDTO> listConsultation() throws ServiceException;

	public List<ConsultationDTO> listConsultationByUserId(int userId, AbleStateEnum state) throws ServiceException;

	public List<ConsultationDTO> listRemindByRemindDate(Date date, int adviserId) throws ServiceException;

}
