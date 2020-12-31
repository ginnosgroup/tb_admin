package org.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.RegionDO;

public interface RegionDAO {

    public RegionDO getRegionById(int id);

    public List<RegionDO> listAllRegion();

    public int insert(RegionDO regionDo);

    public boolean update(@Param("name") String name, @Param("id") int id, @Param("weight") int weight);

    // 获取父区域列表
    List<RegionDO> selectByParent();

    // 根据父区域获取子区域
    List<RegionDO> selectByParentId(Integer parentId);

    List<RegionDO> regionList();

    int updateFinanceBankId(@Param("id") Integer regionId, @Param("financeBankId") Integer financeBankId);
}
