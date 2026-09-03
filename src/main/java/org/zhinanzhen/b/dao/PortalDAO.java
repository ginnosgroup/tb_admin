package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.PortalDO;

public interface PortalDAO {

	int addPortal(PortalDO portalDo);

	int updatePortal(PortalDO portalDo);

	int clearGeneratedDocumentPaths(@Param("id") int id);

	int updatePortalStateIfCurrent(@Param("id") int id, @Param("fromState") String fromState,
			@Param("toState") String toState);

	List<PortalDO> listPortal(@Param("typeId") Integer typeId, @Param("strState") String strState,
			@Param("keyword") String keyword, @Param("offset") int offset, @Param("rows") int rows,
			@Param("adviserId") Integer adviserId, @Param("adviserRegionId") Integer adviserRegionId,
			@Param("officialId") Integer officialId, @Param("officialRegionId") Integer officialRegionId,
			@Param("maraId") Integer maraId);

	int countPortal(@Param("typeId") Integer typeId, @Param("strState") String strState,
			@Param("keyword") String keyword, @Param("adviserId") Integer adviserId,
			@Param("adviserRegionId") Integer adviserRegionId, @Param("officialId") Integer officialId,
			@Param("officialRegionId") Integer officialRegionId, @Param("maraId") Integer maraId);

	PortalDO getPortalById(@Param("id") int id, @Param("adviserId") Integer adviserId,
			@Param("adviserRegionId") Integer adviserRegionId, @Param("officialId") Integer officialId,
			@Param("officialRegionId") Integer officialRegionId, @Param("maraId") Integer maraId);

	PortalDO getPortalByTypeIdAndName(@Param("typeId") int typeId, @Param("name") String name);

	int deletePortal(int id);

	int updateAiConsultContent(@Param("id") int id, @Param("aiConsultContent") String aiConsultContent);

}
