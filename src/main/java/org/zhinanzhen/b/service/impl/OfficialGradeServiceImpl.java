package org.zhinanzhen.b.service.impl;

import com.ikasoa.core.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.OfficialGradeDao;
import org.zhinanzhen.b.dao.pojo.OfficialGradeDO;
import org.zhinanzhen.b.service.OfficialGradeService;
import org.zhinanzhen.b.service.pojo.OfficialGradeDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("OfficialGradeService")
public class OfficialGradeServiceImpl extends BaseService implements OfficialGradeService {

    @Resource
    private OfficialGradeDao officialGradeDao;

    @Override
    public List<OfficialGradeDTO> getOfficialGrade() throws ServiceException {
        List<OfficialGradeDTO> officialGradeDTOList =new ArrayList<>();
        try {
            List<OfficialGradeDO> officialGradeDOList = officialGradeDao.getOfficialGrade();
            if (officialGradeDOList == null)
                return null;
            officialGradeDOList.forEach(
                    officialGradeDO -> officialGradeDTOList.add(mapper.map(officialGradeDO,OfficialGradeDTO.class))
            );


        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        return officialGradeDTOList;

    }

    @Override
    public int addOfficialGrade(OfficialGradeDTO officialGradeDtO) throws ServiceException {
        if (officialGradeDtO == null) {
            ServiceException se = new ServiceException("officialTagDto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        OfficialGradeDO officialGradeDO = mapper.map(officialGradeDtO, OfficialGradeDO.class);
        return officialGradeDao.addOfficialGrade(officialGradeDO) > 0 ? officialGradeDO.getId() : 0;

    }
}
