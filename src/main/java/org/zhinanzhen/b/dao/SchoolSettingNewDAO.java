package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SchoolSettingNewDO;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/08/30 下午 5:52
 * Description:
 * Version: V1.0
 */
public interface SchoolSettingNewDAO {

    List<SchoolSettingNewDO> list(@Param("providerId")int providerId , @Param("isDelete")Boolean isDelete);

    /**
     * 添加的记录中 gmt_create 字段是 NOW()
     * @param schoolSettingNewDO
     * @return
     */
    int add(SchoolSettingNewDO schoolSettingNewDO);

    /**
     * 添加的记录中 gmt_create 字段不是 NOW()
     * @param schoolSettingNewDO
     * @return
     */
    int add1(SchoolSettingNewDO schoolSettingNewDO);

    List<SchoolSettingNewDO> getByProviderIdAndLevel(@Param("providerId")int providerId, @Param("level")Integer level,
                                               @Param("courseLevel")String courseLevel, @Param("courseId")Integer courseId,
                                               @Param("isDelete")Boolean isDelete);

    int delete(int id);

    SchoolSettingNewDO getSchoolSettingNewById(int id);
}
