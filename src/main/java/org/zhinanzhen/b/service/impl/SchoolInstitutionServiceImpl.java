package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.SchoolCourseDAO;
import org.zhinanzhen.b.dao.SchoolInstitutionDAO;
import org.zhinanzhen.b.dao.SchoolInstitutionLocationDAO;
import org.zhinanzhen.b.dao.pojo.SchoolCourseDO;
import org.zhinanzhen.b.dao.pojo.SchoolInstitutionDO;
import org.zhinanzhen.b.dao.pojo.SchoolInstitutionLocationDO;
import org.zhinanzhen.b.service.SchoolInstitutionService;
import org.zhinanzhen.b.service.pojo.SchoolCourseDTO;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionDTO;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionLocationDTO;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 17:56
 * Description:
 * Version: V1.0
 */
@Service
public class SchoolInstitutionServiceImpl extends BaseService implements SchoolInstitutionService{

    @Resource
    private SchoolInstitutionDAO schoolInstitutionDAO;

    @Resource
    private SchoolInstitutionLocationDAO schoolInstitutionLocationDAO;

    @Resource
    private SchoolCourseDAO schoolCourseDAO;

    @Override
    public List<SchoolInstitutionDTO> listSchoolInstitutionDTO(String name, String type, String code, int pageNum, int pageSize) {
        if ( pageNum < 0 )
            pageNum = DEFAULT_PAGE_NUM;
        if ( pageSize < 0 )
            pageSize = DEFAULT_PAGE_SIZE;
        List<SchoolInstitutionDO> schoolInstitutionDOS = schoolInstitutionDAO.listSchoolInstitutionDO(name,type, code, pageNum * pageSize, pageSize);
        List<SchoolInstitutionDTO> schoolInstitutionDTOS = new ArrayList<>();
        if (schoolInstitutionDOS == null)
            return null;
        for (SchoolInstitutionDO si : schoolInstitutionDOS){
            SchoolInstitutionDTO schoolInstitutionDTO = mapper.map(si,SchoolInstitutionDTO.class);
            //schoolInstitutionDTO = putSchoolInfo(schoolInstitutionDTO,si.getId(),null);
            schoolInstitutionDTOS.add(schoolInstitutionDTO);
        }
        return schoolInstitutionDTOS;
    }

    @Override
    public SchoolInstitutionDTO getSchoolInstitutionById(Integer id) {
        SchoolInstitutionDO schoolInstitutionDO = schoolInstitutionDAO.getSchoolInstitutionById(id);
        if (schoolInstitutionDO == null)
            return  null;
        SchoolInstitutionDTO schoolInstitutionDTO = mapper.map(schoolInstitutionDO,SchoolInstitutionDTO.class);
        schoolInstitutionDTO = putSchoolInfo(schoolInstitutionDTO,schoolInstitutionDO.getId(),null);
        return schoolInstitutionDTO;
    }

    @Override
    public SchoolInstitutionDTO getSchoolInstitutionByCode(String code) {
        SchoolInstitutionDO schoolInstitutionDO = schoolInstitutionDAO.getSchoolInstitutionByCode(code);
        if (schoolInstitutionDO == null)
            return  null;
        SchoolInstitutionDTO schoolInstitutionDTO = mapper.map(schoolInstitutionDO,SchoolInstitutionDTO.class);
        schoolInstitutionDTO = putSchoolInfo(schoolInstitutionDTO,schoolInstitutionDO.getId(),null);
        return schoolInstitutionDTO;
    }

    @Override
    public int count(String name, String type) {
        return schoolInstitutionDAO.count(name,type);
    }

    @Override
    public boolean update(SchoolInstitutionDTO schoolInstitutionDTO) {
        SchoolInstitutionDO schoolInstitutionDO = mapper.map(schoolInstitutionDTO,SchoolInstitutionDO.class);
        return schoolInstitutionDAO.update(schoolInstitutionDO);
    }

    @Override
    public int add(SchoolInstitutionDTO schoolInstitutionDTO) {
        SchoolInstitutionDO schoolInstitutionDO = mapper.map(schoolInstitutionDTO,SchoolInstitutionDO.class);
        if (schoolInstitutionDAO.add(schoolInstitutionDO) > 0)
            schoolInstitutionDTO.setId(schoolInstitutionDO.getId());
        return schoolInstitutionDTO.getId();
    }

    @Override
    public boolean delete(int id) {
        return schoolInstitutionDAO.delete(id);
    }

    public SchoolInstitutionDTO putSchoolInfo(SchoolInstitutionDTO schoolInstitutionDTO ,int providerId,String providerCode){
        List<SchoolCourseDO> listSchoolCourse = schoolCourseDAO.listSchoolCourse(providerId,providerCode,null,null);
        if (listSchoolCourse.size() > 0){
            List<SchoolCourseDTO> schoolCourseDTOS = new ArrayList<>();
            for (SchoolCourseDO sc : listSchoolCourse){
                SchoolCourseDTO schoolCourseDTO = mapper.map(sc,SchoolCourseDTO.class);
                schoolCourseDTOS.add(schoolCourseDTO);
            }
            schoolInstitutionDTO.setSchoolCourseDTOS(schoolCourseDTOS);
        }

        List<SchoolInstitutionLocationDO> listSchoolInstitutionLocation = schoolInstitutionLocationDAO.listSchoolInstitutionLocation(providerId,providerCode);
        if (listSchoolInstitutionLocation.size() > 0){
            List<SchoolInstitutionLocationDTO> schoolInstitutionLocationDTOS = new ArrayList<>();
            for (SchoolInstitutionLocationDO sil : listSchoolInstitutionLocation){
                SchoolInstitutionLocationDTO schoolInstitutionLocationDTO = mapper.map(sil,SchoolInstitutionLocationDTO.class);
                schoolInstitutionLocationDTOS.add(schoolInstitutionLocationDTO);
            }
            schoolInstitutionDTO.setSchoolInstitutionLocationDTOS(schoolInstitutionLocationDTOS);
        }
        return schoolInstitutionDTO;
    }
}
