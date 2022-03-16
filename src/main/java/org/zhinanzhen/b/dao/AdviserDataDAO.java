package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.AdviserCommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.AdviserServiceOrderDO;
import org.zhinanzhen.b.dao.pojo.AdviserUserDO;
import org.zhinanzhen.b.dao.pojo.AdviserVisaDO;

public interface AdviserDataDAO {

	List<AdviserServiceOrderDO> listServiceOrder(@Param("adviserId") Integer adviserId);

	List<AdviserVisaDO> listVisa(@Param("adviserId") Integer adviserId);

	List<AdviserCommissionOrderDO> listCommissionOrder(@Param("adviserId") Integer adviserId);

	List<AdviserUserDO> listUser(@Param("adviserId") Integer adviserId);

}
