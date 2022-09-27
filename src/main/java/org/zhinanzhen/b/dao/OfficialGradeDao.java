package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.OfficialGradeDO;

import java.util.List;

public interface OfficialGradeDao {

    List<OfficialGradeDO> listOfficialGrade(@Param("offset") int offset, @Param("rows") int rows);

    int addOfficialGrade(OfficialGradeDO officialGradeDO);

    OfficialGradeDO getOfficialGradeByGrade(String grade);

    OfficialGradeDO getOfficialGradeById(int id);

    int updateOfficialGradeById(OfficialGradeDO officialGradeDO);

    int updateOfficialGradeNameById(@Param("grade") String grade,@Param("id") int id);

    int deleteOfficialGradeById(@Param("id") Integer id);
}
