package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.OfficialGradeDO;
import org.zhinanzhen.b.service.pojo.OfficialGradeDTO;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

public interface OfficialGradeService {

    List<OfficialGradeDTO> getOfficialGrade() throws ServiceException;

    int addOfficialGrade(OfficialGradeDTO officialGradeDtO) throws ServiceException;
}
