package org.zhinanzhen.b.dao;

import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;

public interface CommissionOrderDAO {

	int addCommissionOrder(CommissionOrderDO commissionOrderDo);

	int updateCommissionOrder(CommissionOrderDO commissionOrderDo);
	
	CommissionOrderDO getCommissionOrderById(int id);

}
