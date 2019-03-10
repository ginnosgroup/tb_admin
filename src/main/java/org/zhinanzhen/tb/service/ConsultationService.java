package org.zhinanzhen.tb.service;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.tb.service.pojo.ConsultationDTO;

public interface ConsultationService {

	public int addConsultation(ConsultationDTO consultationDto) throws ServiceException;

	public List<ConsultationDTO> listConsultationByUserId(int userId) throws ServiceException;

	public List<ConsultationDTO> listRemindByRemindDate(Date date) throws ServiceException;

}
