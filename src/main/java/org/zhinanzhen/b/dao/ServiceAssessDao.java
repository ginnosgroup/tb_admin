package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ServiceAssessDO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/11/25 17:05
 * Description:
 * Version: V1.0
 */
public interface ServiceAssessDao {

    List<ServiceAssessDO> list(@Param("serviceId")Integer serviceId);

    int add(@Param("name") String name, @Param("serviceId") Integer serviceId);

    int update(@Param("id") Integer id, @Param("name") String name);

    int delete(@Param("id") Integer id);

    ServiceAssessDO seleteAssessById(@Param("id") String id);

    List<ServiceAssessDO> seleteAssessByServiceId(@Param("serviceId") String serviceId);
}
