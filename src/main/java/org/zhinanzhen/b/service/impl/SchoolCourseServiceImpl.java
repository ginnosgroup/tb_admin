package org.zhinanzhen.b.service.impl;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.SchoolCourseDAO;
import org.zhinanzhen.b.dao.SchoolSettingNewDAO;
import org.zhinanzhen.b.dao.pojo.SchoolCourseDO;
import org.zhinanzhen.b.dao.pojo.SchoolSettingNewDO;
import org.zhinanzhen.b.service.SchoolCourseService;
import org.zhinanzhen.b.service.pojo.SchoolCourseDTO;
import org.zhinanzhen.b.service.pojo.SchoolSettingNewDTO;
import org.zhinanzhen.tb.service.impl.BaseService;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/07 10:47
 * Description:
 * Version: V1.0
 */
@Service
public class SchoolCourseServiceImpl extends BaseService implements SchoolCourseService {

    @Resource
    private SchoolCourseDAO schoolCourseDAO;

    @Resource
    private SchoolSettingNewDAO schoolSettingNewDAO;

    @Override
    public List<SchoolCourseDTO> list(Integer providerId, String providerCode,Boolean isFreeze, String courseLevel, String courseCode, int pageNum, int pageSize) {
        if (pageNum < 0 )
            pageNum = DEFAULT_PAGE_NUM;
        if (pageSize < 0 )
            pageSize = DEFAULT_PAGE_SIZE;
        List<SchoolCourseDO>  schoolCourseDOList = schoolCourseDAO.listSchoolCourse(providerId,providerCode,isFreeze,courseLevel, null,courseCode,
                pageNum*pageSize, pageSize);
        List<SchoolCourseDTO> schoolCourseDTOList = new ArrayList<>();
        if (schoolCourseDOList.size() > 0 )
            schoolCourseDOList.forEach(schoolCourseDO -> {
                schoolCourseDTOList.add(mapper.map(schoolCourseDO, SchoolCourseDTO.class));
            });
        return schoolCourseDTOList;
    }

    @Override
    public int count(Integer providerId, String providerCode, Boolean isFreeze) {
        return schoolCourseDAO.count(providerId,providerCode, isFreeze);
    }

    @Override
    public SchoolCourseDTO schoolCourseById(int id) {
        SchoolCourseDO schoolCourseDO = schoolCourseDAO.schoolCourseById(id);
        if (schoolCourseDO != null)
            return mapper.map(schoolCourseDO,SchoolCourseDTO.class);
        else
            return null;
    }

    @Override
    public boolean delete(int id) {
        return schoolCourseDAO.freeze(id);
    }

    @Override
    public int add(SchoolCourseDTO schoolCourseDTO) {
        if (schoolCourseDTO != null){
            SchoolCourseDO schoolCourseDO = mapper.map(schoolCourseDTO,SchoolCourseDO.class);
            if (schoolCourseDAO.add(schoolCourseDO) > 0 ){
                schoolCourseDTO.setId(schoolCourseDO.getId());
                return schoolCourseDTO.getId();
            }
        }
        return 0;
    }

    @Override
    public boolean update(SchoolCourseDTO schoolCourseDTO) {
        if (schoolCourseDTO != null){
            SchoolCourseDO schoolCourseDO = mapper.map(schoolCourseDTO,SchoolCourseDO.class);
            return schoolCourseDAO.update(schoolCourseDO);
        }
        return false;
    }

    @Override
    public List<String> getCourseLevelList(int providerId) {
        return schoolCourseDAO.getCourseLevelList(providerId);
    }

    @Override
    public List<SchoolCourseDTO> getCourseToSetting(int providerId, String courseLevel, String courseName, String courseCode, int pageNum, int pageSize) {
        if (pageNum < 0 )
            pageNum = DEFAULT_PAGE_NUM;
        if (pageSize < 0 )
            pageSize = DEFAULT_PAGE_SIZE;
        List<SchoolCourseDO> schoolCourseDOlist = schoolCourseDAO.listSchoolCourse(providerId,null,false,courseLevel, courseName,courseCode,
                pageNum*pageSize, pageSize);
        List<SchoolCourseDTO> schoolCourseDTOlist = new ArrayList<>();
        schoolCourseDOlist.forEach(schoolCourseDO -> {
            SchoolCourseDTO schoolCourseDTO = mapper.map(schoolCourseDO, SchoolCourseDTO.class);
            SchoolSettingNewDO schoolSettingNewDO = returnSetting(schoolCourseDO);

            if (schoolSettingNewDO != null)
                schoolCourseDTO.setSchoolSettingNewDTO(mapper.map(schoolSettingNewDO, SchoolSettingNewDTO.class));

            schoolCourseDTOlist.add(schoolCourseDTO);
        });
        return schoolCourseDTOlist;
    }

    public SchoolSettingNewDO returnSetting(SchoolCourseDO schoolCourseDO){
        SchoolSettingNewDO schoolSettingNewDO = null;
        List<SchoolSettingNewDO> schoolSettingNewDOList = new ArrayList<>();
        if (schoolCourseDO != null){
            schoolSettingNewDOList = schoolSettingNewDAO.getByProviderIdAndLevel(schoolCourseDO.getProviderId(),
                    3,null,schoolCourseDO.getId(), false);
            for (SchoolSettingNewDO setting : schoolSettingNewDOList){
                if (setting.getStartDate().before(new Date()) && setting.getEndDate().after(new Date())){
                    schoolSettingNewDO = setting;
                    break;
                }
            }
            if (schoolSettingNewDO != null)
                return schoolSettingNewDO;

            schoolSettingNewDOList = schoolSettingNewDAO.getByProviderIdAndLevel(schoolCourseDO.getProviderId(),
                    2,schoolCourseDO.getCourseLevel(),null, false);
            for (SchoolSettingNewDO setting : schoolSettingNewDOList){
                if (setting.getStartDate().before(new Date()) && setting.getEndDate().after(new Date())){
                    schoolSettingNewDO = setting;
                    break;
                }
            }
            if (schoolSettingNewDO != null && schoolSettingNewDO.getCourseLevel().equalsIgnoreCase(schoolCourseDO.getCourseLevel()))
                return schoolSettingNewDO;

            schoolSettingNewDOList = schoolSettingNewDAO.getByProviderIdAndLevel(schoolCourseDO.getProviderId(),
                    1,null,null, false);
            for (SchoolSettingNewDO setting : schoolSettingNewDOList){
                if (setting.getStartDate().before(new Date()) && setting.getEndDate().after(new Date())){
                    schoolSettingNewDO = setting;
                    break;
                }
            }
            if (schoolSettingNewDO != null)
                return schoolSettingNewDO;
        }
        return schoolSettingNewDO;
    }
}
