package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.PortalSupplementDO;

public interface PortalSupplementDAO {

	int addPortalSupplement(PortalSupplementDO supplement);

	PortalSupplementDO getPortalSupplementById(@Param("id") int id);

	PortalSupplementDO getPortalSupplementByFilters(@Param("id") int id, @Param("portalId") Integer portalId,
			@Param("name") String name);

	PortalSupplementDO getPortalSupplementForUpdate(@Param("id") int id);

	List<PortalSupplementDO> listPortalSupplementByPortalId(@Param("portalId") int portalId);

	List<PortalSupplementDO> listPortalSupplement(@Param("id") Integer id, @Param("portalId") Integer portalId,
			@Param("maraId") Integer maraId, @Param("officialId") Integer officialId,
			@Param("strState") String strState, @Param("name") String name,
			@Param("maraVisibleOnly") boolean maraVisibleOnly,
			@Param("offset") int offset, @Param("rows") int rows);

	int countPortalSupplement(@Param("id") Integer id, @Param("portalId") Integer portalId,
			@Param("maraId") Integer maraId, @Param("officialId") Integer officialId,
			@Param("strState") String strState, @Param("name") String name,
			@Param("maraVisibleOnly") boolean maraVisibleOnly);

	int getMaxRoundNoByPortalId(@Param("portalId") int portalId);

	int updatePortalSupplement(PortalSupplementDO supplement);

	int deletePortalSupplementById(@Param("id") int id);
}
