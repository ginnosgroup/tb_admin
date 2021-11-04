package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.SchoolInstitutionLocationDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/08 10:30
 * Description:
 * Version: V1.0
 */
public interface SchoolInstitutionLocationService {

    List<SchoolInstitutionLocationDTO> list(Integer providerId, String providerCode);

    SchoolInstitutionLocationDTO getById(int id);

    int add(SchoolInstitutionLocationDTO schoolInstitutionLocationDTO);

    boolean update(SchoolInstitutionLocationDTO schoolInstitutionLocationDTO);

    boolean delete(int id);

    List<String> getState();
}
