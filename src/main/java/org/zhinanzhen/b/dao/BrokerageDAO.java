package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.BrokerageDO;

public interface BrokerageDAO {
	
	public int addBrokerage(BrokerageDO brokerageDo);

	public int updateBrokerage(BrokerageDO brokerageDo);

	public int countBrokerage(@Param("stardDate") String stardDate, @Param("endDate") String endDate, @Param("adviserId") Integer adviserId);

	public List<BrokerageDO> listBrokerage(@Param("stardDate") String stardDate, @Param("endDate") String endDate, @Param("adviserId") Integer adviserId,
			@Param("offset") int offset, @Param("rows") int rows);

	public BrokerageDO getBrokerageById(int id);

}
