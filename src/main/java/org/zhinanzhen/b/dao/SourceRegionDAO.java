package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SourceRegionDO;

public interface SourceRegionDAO {

	int addSourceRegion(SourceRegionDO sourceRegionDo);

	int updateSourceRegion(SourceRegionDO sourceRegionDo);

	List<SourceRegionDO> listSourceRegion(@Param("parentId") Integer parentId);

	int deleteSourceRegion(int id);

}
