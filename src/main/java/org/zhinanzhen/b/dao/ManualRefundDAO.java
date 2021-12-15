package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ManualRefundDO;

public interface ManualRefundDAO {

	int addManualRefund(ManualRefundDO manualRefundDo);

	List<ManualRefundDO> listManualRefund(@Param("type") String type, @Param("state") String state);
	
	ManualRefundDO getManualRefundById(int id);

	int updateManualRefund(ManualRefundDO manualRefundDo);
	
	int deleteManualRefundById(int id);

}
