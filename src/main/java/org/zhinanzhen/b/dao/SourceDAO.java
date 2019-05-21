package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SourceDO;

public interface SourceDAO {
	
	int addSource(SourceDO sourceDo);

	int updateSource(SourceDO sourceDo);

	List<SourceDO> listSource(@Param("sourceRegionId") Integer sourceRegionId);

	int deleteSource(int id);

}
