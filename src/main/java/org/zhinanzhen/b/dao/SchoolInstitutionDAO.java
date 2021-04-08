package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.SchoolInstitutionDO;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 15:49
 * Description:
 * Version: V1.0
 */
public interface SchoolInstitutionDAO {

    List<SchoolInstitutionDO> listSchoolInstitutionDO(@Param("name") String name, @Param("type") String type,
                                                      @Param("offset") int offset, @Param("rows") int rows);

    SchoolInstitutionDO getSchoolInstitutionById(@Param("id") Integer id);

    int count(@Param("name") String name, @Param("type") String type);

    boolean update(SchoolInstitutionDO schoolInstitutionDO);

    SchoolInstitutionDO getSchoolInstitutionByCode(@Param("code") String code);

    int add(SchoolInstitutionDO schoolInstitutionDO);

    boolean delete(@Param("id") int id);
}
