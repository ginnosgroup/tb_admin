package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.PortalLogDO;

public interface PortalLogDAO {

	int addPortalLog(PortalLogDO portalLogDo);

	List<PortalLogDO> listPortalLog(@Param("portalId") Integer portalId, @Param("offset") int offset,
			@Param("rows") int rows);

	List<PortalLogDO> listPortalLogBySupplementId(@Param("supplementId") int supplementId);

	int countPortalLog(@Param("portalId") Integer portalId);

}
