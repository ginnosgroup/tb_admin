package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderListDO;

public interface CommissionOrderDAO {

	int addCommissionOrder(CommissionOrderDO commissionOrderDo);

	public int countCommissionOrder(@Param("adviserId") Integer adviserId, @Param("name") String name,
			@Param("phone") String phone, @Param("wechatUsername") String wechatUsername,
			@Param("schoolId") Integer schoolId, @Param("isSettle") Boolean isSettle, @Param("state") String state);

	public List<CommissionOrderListDO> listCommissionOrder(@Param("adviserId") Integer adviserId,
			@Param("name") String name, @Param("phone") String phone, @Param("wechatUsername") String wechatUsername,
			@Param("schoolId") Integer schoolId, @Param("isSettle") Boolean isSettle, @Param("state") String state,
			@Param("offset") int offset, @Param("rows") int rows);

	int updateCommissionOrder(CommissionOrderDO commissionOrderDo);

	CommissionOrderDO getCommissionOrderById(int id);

}
