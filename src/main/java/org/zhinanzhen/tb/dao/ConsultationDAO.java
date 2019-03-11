package org.zhinanzhen.tb.dao;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.tb.dao.pojo.ConsultationDO;

public interface ConsultationDAO {

	public int addConsultation(ConsultationDO consultationDo);
	
	public int updateConsultation(ConsultationDO consultationDo);

	public List<ConsultationDO> listConsultationByUserId(Integer userId);

	public List<ConsultationDO> listConsultationByRemindDate(Date date);

}
