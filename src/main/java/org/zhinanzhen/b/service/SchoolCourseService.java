package org.zhinanzhen.b.service;


import org.zhinanzhen.b.dao.pojo.SchoolCourseDO;
import org.zhinanzhen.b.service.pojo.SchoolCourseDTO;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/07 10:47
 * Description:
 * Version: V1.0
 */
public interface SchoolCourseService {

    List<SchoolCourseDTO> list(Integer providerId, String providerCode,Boolean isFreeze, int pageNum, int pageSize);

    int count(Integer providerId, String providerCode, Boolean isFreeze);

    SchoolCourseDTO schoolCourseById(int id);

    boolean delete(int id);

    int add(SchoolCourseDTO schoolCourseDTO);

    boolean update(SchoolCourseDTO schoolCourseDTO);
}