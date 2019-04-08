package org.zhinanzhen.tb.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.ConsultationDO;

public interface ConsultationDAO {

	public int addConsultation(ConsultationDO consultationDo);

	public int updateConsultation(ConsultationDO consultationDo);

	public List<ConsultationDO> listConsultationByUserId(Integer userId);

	public List<ConsultationDO> listConsultationByRemindDate(@Param("date") Date date,
			@Param("adviserId") Integer adviserId);

}
