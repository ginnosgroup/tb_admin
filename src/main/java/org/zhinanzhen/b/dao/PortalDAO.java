package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.PortalDO;

public interface PortalDAO {

	int addPortal(PortalDO portalDo);

	int updatePortal(PortalDO portalDo);

	List<PortalDO> listPortal(@Param("typeId") Integer typeId, @Param("strState") String strState,
			@Param("keyword") String keyword, @Param("offset") int offset, @Param("rows") int rows);

	int countPortal(@Param("typeId") Integer typeId, @Param("strState") String strState,
			@Param("keyword") String keyword);

	PortalDO getPortalById(@Param("id") int id);

	int deletePortal(int id);

}
