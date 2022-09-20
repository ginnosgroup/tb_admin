package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.CommissionOrderCommentDO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderListDO;

import java.util.List;

public interface CommissionOrderCommentDAO {

	int add(CommissionOrderCommentDO commissionOrderCommentDo);

	public List<CommissionOrderCommentDO> list(@Param("commissionOrderId") Integer commissionOrderId);

	public int delete(int id);

}
