package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.BrokerageSaDO;

public interface BrokerageSaDAO {

	public int addBrokerageSa(BrokerageSaDO brokerageSaDo);

	public int updateBrokerageSa(BrokerageSaDO brokerageSaDo);

//	public int countBrokerageSa(@Param("keyword") String keyword, @Param("stardDate") String stardDate,
//			@Param("endDate") String endDate, @Param("adviserId") Integer adviserId);
//
//	public List<BrokerageSaDO> listBrokerageSa(@Param("keyword") String keyword, @Param("stardDate") String stardDate,
//			@Param("endDate") String endDate, @Param("adviserId") Integer adviserId, @Param("offset") int offset,
//			@Param("rows") int rows);

	public BrokerageSaDO getBrokerageSaById(int id);

	public int deleteBrokerageSaById(int id);

}
