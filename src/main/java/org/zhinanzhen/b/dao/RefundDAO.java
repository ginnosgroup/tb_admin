package org.zhinanzhen.b.dao;

import org.zhinanzhen.b.dao.pojo.RefundDO;

public interface RefundDAO {

	public int addRefund(RefundDO refundDO);

	public int updateRefund(RefundDO refundDO);

	public int deleteRefund(int id);

}
