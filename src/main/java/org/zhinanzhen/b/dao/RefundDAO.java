package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.RefundDO;
import org.zhinanzhen.b.dao.pojo.RefundListDO;

public interface RefundDAO {

	public int addRefund(RefundDO refundDo);

	public int updateRefund(RefundDO refundDo);

	public int countRefund(@Param("keyword") String keyword, @Param("startHandlingDate") String startHandlingDate,
			@Param("endHandlingDate") String endHandlingDate, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId);

	public List<RefundListDO> listRefund(@Param("keyword") String keyword,
			@Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
			@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("adviserId") Integer adviserId, @Param("officialId") Integer officialId, @Param("offset") int offset,
			@Param("rows") int rows);

	public RefundDO getRefundById(int id);

	public int deleteRefund(int id);

}
