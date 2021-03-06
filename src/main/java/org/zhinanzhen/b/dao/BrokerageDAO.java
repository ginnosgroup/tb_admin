package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.BrokerageDO;
import org.zhinanzhen.b.dao.pojo.BrokerageListDO;

public interface BrokerageDAO {

	public int addBrokerage(BrokerageDO brokerageDo);

	public int updateBrokerage(BrokerageDO brokerageDo);

	public int countBrokerage(@Param("keyword") String keyword, @Param("startHandlingDate") String startHandlingDate,
			@Param("endHandlingDate") String endHandlingDate, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("adviserId") Integer adviserId, @Param("userId") Integer userId);

	public List<BrokerageListDO> listBrokerage(@Param("keyword") String keyword,
			@Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
			@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("adviserId") Integer adviserId, @Param("userId") Integer userId, @Param("offset") int offset,
			@Param("rows") int rows);

	public Double sumBonusByThisMonth(@Param("adviserId") Integer adviserId);

	public BrokerageDO getBrokerageById(int id);

	public int deleteBrokerageById(int id);

}
