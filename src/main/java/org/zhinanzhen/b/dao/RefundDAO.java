package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.RefoundReportDO;
import org.zhinanzhen.b.dao.pojo.RefundDO;

public interface RefundDAO {

	int addRefund(RefundDO refundDo);

	List<RefundDO> listRefund(@Param("adviserId") Integer adviserId, @Param("type") String type,
			@Param("state") String state);

	RefundDO getRefundById(int id);

	RefundDO getRefundByCommissionOrderId(int commissionOrderId);

	RefundDO getRefundByVisaId(int visaId);

	int updateRefund(RefundDO refundDo);

	int deleteRefundById(int id);

    List<RefoundReportDO> listRefundReport(@Param("startDate") String startDate, @Param("endDate") String endDate,
										   @Param("dateType")String dateType, @Param("dateMethod")String dateMethod,
										   @Param("regionId")Integer regionId, @Param("adviserId")Integer adviserId,
										   @Param("adviserIdList")List<String> adviserIdList);
}
