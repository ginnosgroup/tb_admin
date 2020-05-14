package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.CommissionOrderCommentDO;

public interface CommissionOrderCommentDAO {

	int add(CommissionOrderCommentDO commissionOrderCommentDo);

	public List<CommissionOrderCommentDO> list(@Param("commissionOrderId") Integer commissionOrderId);

	public int delete(int id);

}
