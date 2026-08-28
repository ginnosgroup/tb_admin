package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.PortalTypeDO;

public interface PortalTypeDAO {

	int addPortalType(PortalTypeDO portalTypeDo);

	int updatePortalType(PortalTypeDO portalTypeDo);

	List<PortalTypeDO> listPortalType(@Param("isDelete") Integer isDelete, @Param("keyword") String keyword);

	PortalTypeDO getPortalTypeById(@Param("id") int id);

	int deletePortalType(int id);

}
