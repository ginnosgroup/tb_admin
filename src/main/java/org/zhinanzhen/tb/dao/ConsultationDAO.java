package org.zhinanzhen.tb.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.ConsultationDO;

public interface ConsultationDAO {

	public int addConsultation(ConsultationDO consultationDo);

	public int updateConsultation(ConsultationDO consultationDo);

	public List<ConsultationDO> listConsultation(); // 返回所有，包括不显示的

	public List<ConsultationDO> listConsultationByUserId(@Param("userId") Integer userId, @Param("state") String state);

	public List<ConsultationDO> listConsultationByRemindDate(@Param("date") Date date,
			@Param("adviserId") Integer adviserId);

}
