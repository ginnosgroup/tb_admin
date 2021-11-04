package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.SchoolInstitutionLocationDAO;
import org.zhinanzhen.b.dao.pojo.SchoolInstitutionLocationDO;
import org.zhinanzhen.b.service.SchoolInstitutionLocationService;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionLocationDTO;
import org.zhinanzhen.tb.service.impl.BaseService;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/08 10:30
 * Description:
 * Version: V1.0
 */
@Service
public class SchoolInstitutionLocationServiceImpl extends BaseService implements SchoolInstitutionLocationService {

    @Resource
    private SchoolInstitutionLocationDAO schoolInstitutionLocationDAO;

    @Override
    public List<SchoolInstitutionLocationDTO> list(Integer providerId, String providerCode) {
        List<SchoolInstitutionLocationDO> institutionLocationDOS = schoolInstitutionLocationDAO.listSchoolInstitutionLocation(providerId,providerCode);
        List<SchoolInstitutionLocationDTO> institutionLocationDTOS = new ArrayList<>();
        if (institutionLocationDOS.size() > 0)
            institutionLocationDOS.forEach(institutionLocationDO -> {
                institutionLocationDTOS.add(mapper.map(institutionLocationDO,SchoolInstitutionLocationDTO.class));
            });
        return institutionLocationDTOS;
    }

    @Override
    public SchoolInstitutionLocationDTO getById(int id) {
        SchoolInstitutionLocationDO schoolInstitutionLocationDO = schoolInstitutionLocationDAO.getById(id);
        if (schoolInstitutionLocationDO != null)
            return mapper.map(schoolInstitutionLocationDO,SchoolInstitutionLocationDTO.class);
        return null;
    }

    @Override
    public int add(SchoolInstitutionLocationDTO schoolInstitutionLocationDTO) {
        if (schoolInstitutionLocationDTO != null) {
            SchoolInstitutionLocationDO schoolInstitutionLocationDO = mapper.map(schoolInstitutionLocationDTO,SchoolInstitutionLocationDO.class);
            if (schoolInstitutionLocationDAO.add(schoolInstitutionLocationDO) > 0 ){
                schoolInstitutionLocationDTO.setId(schoolInstitutionLocationDO.getId());
                return schoolInstitutionLocationDTO.getId();
            }
        }
        return 0;
    }

    @Override
    public boolean update(SchoolInstitutionLocationDTO schoolInstitutionLocationDTO) {
        if (schoolInstitutionLocationDTO != null) {
            SchoolInstitutionLocationDO schoolInstitutionLocationDO = mapper.map(schoolInstitutionLocationDTO,SchoolInstitutionLocationDO.class);
            return schoolInstitutionLocationDAO.update(schoolInstitutionLocationDO);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        return schoolInstitutionLocationDAO.delete(id);
    }

    @Override
    public List<String> getState() {
        return schoolInstitutionLocationDAO.getState();
    }
}
