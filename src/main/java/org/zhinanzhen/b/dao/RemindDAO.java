package org.zhinanzhen.b.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.RemindDO;

public interface RemindDAO {

	public int addRemind(RemindDO remindDo);

	public List<RemindDO> listRemindBySchoolBrokerageSaId(@Param("schoolBrokerageSaId") int schoolBrokerageSaId,
			@Param("adviserId") Integer adviserId, @Param("state") String state);

	public List<RemindDO> listRemindByVisaId(@Param("visaId") int visaId, @Param("adviserId") Integer adviserId,
			@Param("state") String state);

	public List<RemindDO> listRemindByBrokerageSaId(@Param("brokerageSaId") int brokerageSaId,
			@Param("adviserId") Integer adviserId, @Param("state") String state);

	public List<RemindDO> listRemindByRemindDate(Date date);

	public List<RemindDO> updateStateBySchoolBrokerageSaId(@Param("id") int id, @Param("state") String state);

	public List<RemindDO> updateStateByVisaId(@Param("id") int id, @Param("state") String state);

	public List<RemindDO> updateStateByBrokerageSaId(@Param("id") int id, @Param("state") String state);

	@Deprecated
	public int deleteRemindById(int id);

	public int deleteRemindBySchoolBrokerageSaId(int id);

	public int deleteRemindByVisaId(int id);

	public int deleteRemindByBrokerageSaId(int id);

}
