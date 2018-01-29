package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.BrokerageSaDO;
import org.zhinanzhen.b.dao.pojo.BrokerageSaListDO;

public interface BrokerageSaDAO {

	public int addBrokerageSa(BrokerageSaDO brokerageSaDo);

	public int updateBrokerageSa(BrokerageSaDO brokerageSaDo);

	public int countBrokerageSa(@Param("keyword") String keyword, @Param("startHandlingDate") String startHandlingDate,
			@Param("endHandlingDate") String endHandlingDate, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("adviserId") Integer adviserId,
			@Param("schoolId") Integer schoolId);

	public List<BrokerageSaListDO> listBrokerageSa(@Param("keyword") String keyword,
			@Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
			@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("adviserId") Integer adviserId, @Param("schoolId") Integer schoolId, @Param("offset") int offset,
			@Param("rows") int rows);

	public BrokerageSaDO getBrokerageSaById(int id);

	public int deleteBrokerageSaById(int id);

}
