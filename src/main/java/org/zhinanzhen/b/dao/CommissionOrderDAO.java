package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderListDO;

public interface CommissionOrderDAO {

	int addCommissionOrder(CommissionOrderDO commissionOrderDo);

	public int countCommissionOrder(@Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId, @Param("name") String name, @Param("phone") String phone,
			@Param("wechatUsername") String wechatUsername, @Param("schoolId") Integer schoolId,
			@Param("isSettle") Boolean isSettle, @Param("stateList") List<String> stateList);

	public List<CommissionOrderListDO> listCommissionOrder(@Param("maraId") Integer maraId,
			@Param("adviserId") Integer adviserId, @Param("officialId") Integer officialId, @Param("name") String name,
			@Param("phone") String phone, @Param("wechatUsername") String wechatUsername,
			@Param("schoolId") Integer schoolId, @Param("isSettle") Boolean isSettle,
			@Param("stateList") List<String> stateList, @Param("offset") int offset, @Param("rows") int rows);

	public List<CommissionOrderDO> listCommissionOrderByCode(String code);

	public List<CommissionOrderDO> listCommissionOrderByServiceOrderId(Integer serviceOrderId);

	int updateCommissionOrder(CommissionOrderDO commissionOrderDo);

	CommissionOrderListDO getCommissionOrderById(int id);

}
