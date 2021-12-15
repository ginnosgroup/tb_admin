package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.OrderRefundDO;

public interface OrderRefundDAO {

	int addOrderRefund(OrderRefundDO orderRefundDo);

	List<OrderRefundDO> listOrderRefund(@Param("type") String type, @Param("visaId") Integer visaId,
			@Param("commissionOrderId") Integer commissionOrderId, @Param("state") String state);
	
	OrderRefundDO getOrderRefundById(int id);

	int updateOrderRefund(OrderRefundDO orderRefundDo);

	int deleteOrderRefundById(int id);

}
