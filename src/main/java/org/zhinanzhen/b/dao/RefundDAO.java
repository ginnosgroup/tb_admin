package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.RefundDO;

public interface RefundDAO {

	int addRefund(RefundDO refundDo);

	List<RefundDO> listRefund(@Param("type") String type, @Param("state") String state);
	
	RefundDO getRefundById(int id);
	
	RefundDO getRefundByCommissionOrderId(int commissionOrderId);
	
	RefundDO getRefundByVisaId(int visaId);

	int updateRefund(RefundDO refundDo);
	
	int deleteRefundById(int id);

}
