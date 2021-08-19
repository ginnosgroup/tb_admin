package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SchoolCourseDO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 15:47
 * Description:
 * Version: V1.0
 */
public interface SchoolCourseDAO {

    List<SchoolCourseDO> listSchoolCourse(@Param("providerId") Integer providerId,@Param("providerCode") String providerCode,
                                          @Param("offset") Integer offset,@Param("rows") Integer rows);

    SchoolCourseDO schoolCourseById(int id);

    int count(@Param("providerId") Integer providerId,@Param("providerCode") String providerCode);

    boolean delete(int id);

    boolean freeze(int id);

    int add(SchoolCourseDO schoolCourseDO);

    boolean update(SchoolCourseDO schoolCourseDO);
}
