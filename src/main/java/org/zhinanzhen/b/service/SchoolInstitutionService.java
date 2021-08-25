package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.SchoolInstitutionDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 17:48
 * Description:
 * Version: V1.0
 */
public interface SchoolInstitutionService {

    List<SchoolInstitutionDTO> listSchoolInstitutionDTO(String name, String type, String code,  int pageNum, int pageSize);

    SchoolInstitutionDTO getSchoolInstitutionById(Integer id);

    SchoolInstitutionDTO getSchoolInstitutionByCode(String code);

    int count(String name, String type);

    boolean update(SchoolInstitutionDTO schoolInstitutionDTO);

    int add(SchoolInstitutionDTO schoolInstitutionDTO);

    boolean delete(int id);
}
