package org.zhinanzhen.b.service.impl;

import com.ikasoa.core.ErrorCodeEnum;
import org.dozer.MappingException;
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
    public List<OfficialGradeDTO> listOfficialGrade(int pageNum, int pageSize) throws ServiceException {
        if (pageNum < 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        if (pageSize < 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        List<OfficialGradeDTO> officialGradeDTOList =new ArrayList<>();
        try {
            List<OfficialGradeDO> officialGradeDOList = officialGradeDao.listOfficialGrade(pageNum * pageSize, pageSize);
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
            ServiceException se = new ServiceException("officialGradeDto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        OfficialGradeDO officialGradeDO = mapper.map(officialGradeDtO, OfficialGradeDO.class);
        return officialGradeDao.addOfficialGrade(officialGradeDO) > 0 ? officialGradeDO.getId() : 0;

    }

    @Override
    public OfficialGradeDTO getOfficialGradeByGrade(String grade) throws ServiceException {
//        if (grade < 0) {
//            ServiceException se = new ServiceException("grade error !");
//            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
//            throw se;
//        }
        OfficialGradeDTO officialGradeDTO = new OfficialGradeDTO();
        try {
            OfficialGradeDO officialGradeByGrade = officialGradeDao.getOfficialGradeByGrade(grade);
            if (officialGradeByGrade==null){
                return null;
            }
            officialGradeDTO = mapper.map(officialGradeByGrade,OfficialGradeDTO.class);

        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        return officialGradeDTO;
    }
}
