package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SchoolCourseDO;
import org.zhinanzhen.b.dao.pojo.SchoolInstitutionLocationDO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 16:00
 * Description:
 * Version: V1.0
 */
public interface SchoolInstitutionLocationDAO {

    List<SchoolInstitutionLocationDO> listSchoolInstitutionLocation(@Param("providerId") Integer providerId, @Param("providerCode") String providerCode);

    SchoolInstitutionLocationDO getById(int id);

    int add(SchoolInstitutionLocationDO schoolInstitutionLocationDO);

    boolean delete(int id);

    boolean update(SchoolInstitutionLocationDO schoolInstitutionLocationDO);
}
