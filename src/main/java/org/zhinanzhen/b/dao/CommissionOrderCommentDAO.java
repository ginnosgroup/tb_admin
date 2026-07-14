package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.CommissionOrderCommentDO;

import java.util.List;

public interface CommissionOrderCommentDAO {

	int add(CommissionOrderCommentDO commissionOrderCommentDo);

	public List<CommissionOrderCommentDO> list(@Param("commissionOrderId") Integer commissionOrderId);

	Integer getCommissionOrderIdById(@Param("id") Integer id);

	public int delete(int id);

}
