package org.zhinanzhen.b.dao;

import java.util.List;
import java.util.Date;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.BrokerageSaDO;
import org.zhinanzhen.b.dao.pojo.BrokerageSaListDO;

public interface BrokerageSaDAO {

	public int addBrokerageSa(BrokerageSaDO brokerageSaDo);

	public int updateBrokerageSa(BrokerageSaDO brokerageSaDo);

	public int countBrokerageSa(@Param("keyword") String keyword, @Param("startHandlingDate") String startHandlingDate,
			@Param("endHandlingDate") String endHandlingDate, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("adviserId") Integer adviserId,
			@Param("schoolId") Integer schoolId, @Param("userId") Integer userId);

	public List<BrokerageSaListDO> listBrokerageSa(@Param("keyword") String keyword,
			@Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
			@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("adviserId") Integer adviserId, @Param("schoolId") Integer schoolId, @Param("userId") Integer userId,
			@Param("offset") int offset, @Param("rows") int rows);

	public List<BrokerageSaDO> listBrokerageSa2(@Param("startHandlingDate") Date startHandlingDate,
			@Param("endHandlingDate") Date endHandlingDate, @Param("schoolName") String schoolName);

	public Double sumBonusByThisMonth(@Param("adviserId") Integer adviserId);

	public int countBrokerageSaBySchoolName(@Param("schoolName") String schoolName);

	public Double sumTuitionFeeBySchoolName(@Param("schoolName") String schoolName);

	public BrokerageSaDO getBrokerageSaById(int id);

	public int deleteBrokerageSaById(int id);

}
