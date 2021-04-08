package org.zhinanzhen.b.service.impl;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.SchoolCourseDAO;
import org.zhinanzhen.b.dao.pojo.SchoolCourseDO;
import org.zhinanzhen.b.service.SchoolCourseService;
import org.zhinanzhen.b.service.pojo.SchoolCourseDTO;
import org.zhinanzhen.tb.service.impl.BaseService;
import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Override
    public List<SchoolCourseDTO> list(Integer providerId, String providerCode, int pageNum, int pageSize) {
        if (pageNum < 0 )
            pageNum = DEFAULT_PAGE_NUM;
        if (pageSize < 0 )
            pageSize = DEFAULT_PAGE_SIZE;
        List<SchoolCourseDO>  schoolCourseDOList = schoolCourseDAO.listSchoolCourse(providerId,providerCode,pageNum*pageSize,pageSize);
        List<SchoolCourseDTO> schoolCourseDTOList = new ArrayList<>();
        if (schoolCourseDOList.size() > 0 )
            schoolCourseDOList.forEach(schoolCourseDO -> {
                schoolCourseDTOList.add(mapper.map(schoolCourseDO, SchoolCourseDTO.class));
            });
        return schoolCourseDTOList;
    }

    @Override
    public int count(Integer providerId, String providerCode) {
        return schoolCourseDAO.count(providerId,providerCode);
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
        return schoolCourseDAO.delete(id);
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
}
