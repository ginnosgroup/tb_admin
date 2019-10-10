package org.zhinanzhen.b.dao;

import java.util.List;
import java.util.Date;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SchoolBrokerageSaDO;
import org.zhinanzhen.b.dao.pojo.SchoolBrokerageSaListDO;
import org.zhinanzhen.b.dao.pojo.SchoolBrokerageSaByDashboardListDO;

public interface SchoolBrokerageSaDAO {

	public int addSchoolBrokerageSa(SchoolBrokerageSaDO schoolBrokerageSaDo);

	public int updateSchoolBrokerageSa(SchoolBrokerageSaDO schoolBrokerageSaDo);

	public int countSchoolBrokerageSa(@Param("keyword") String keyword,
			@Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
			@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("adviserId") Integer adviserId, @Param("schoolId") Integer schoolId,
			@Param("subagencyId") Integer subagencyId, @Param("userId") Integer userId,
			@Param("isSettleAccounts") Boolean isSettleAccounts);

	public List<SchoolBrokerageSaListDO> listSchoolBrokerageSa(@Param("keyword") String keyword,
			@Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
			@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("adviserId") Integer adviserId, @Param("schoolId") Integer schoolId,
			@Param("subagencyId") Integer subagencyId, @Param("userId") Integer userId,
			@Param("isSettleAccounts") Boolean isSettleAccounts, @Param("offset") int offset, @Param("rows") int rows);

	public List<SchoolBrokerageSaListDO> listSchoolBrokerageSa2(@Param("startHandlingDate") Date startHandlingDate,
			@Param("endHandlingDate") Date endHandlingDate, @Param("schoolName") String schoolName);

	public List<SchoolBrokerageSaByDashboardListDO> listSchoolBrokerageSaByDashboard(
			@Param("adviserId") Integer adviserId, @Param("offset") int offset, @Param("rows") int rows);

	public Double sumBonusByThisMonth(@Param("adviserId") Integer adviserId);

	public SchoolBrokerageSaDO getSchoolBrokerageSaById(int id);

	public int deleteSchoolBrokerageSaById(int id);

}
