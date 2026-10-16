package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.PortalSupplementDO;

public interface PortalSupplementDAO {

	int addPortalSupplement(PortalSupplementDO supplement);

	PortalSupplementDO getPortalSupplementById(@Param("id") int id);

	PortalSupplementDO getPortalSupplementForUpdate(@Param("id") int id);

	List<PortalSupplementDO> listPortalSupplement(@Param("portalId") Integer portalId,
			@Param("maraId") Integer maraId, @Param("officialId") Integer officialId,
			@Param("offset") int offset, @Param("rows") int rows);

	int countPortalSupplement(@Param("portalId") Integer portalId, @Param("maraId") Integer maraId,
			@Param("officialId") Integer officialId);

	int getMaxRoundNoByPortalId(@Param("portalId") int portalId);

	int updatePortalSupplement(PortalSupplementDO supplement);

	int deletePortalSupplementById(@Param("id") int id);
}
