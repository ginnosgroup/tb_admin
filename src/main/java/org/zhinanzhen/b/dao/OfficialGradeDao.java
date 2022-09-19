package org.zhinanzhen.b.dao;

import org.zhinanzhen.b.dao.pojo.OfficialGradeDO;

import java.util.List;

public interface OfficialGradeDao {

    List<OfficialGradeDO> getOfficialGrade();

    int addOfficialGrade(OfficialGradeDO officialGradeDO);

    OfficialGradeDO getOfficialGradeByGrade(int grade);

}
