package org.zhinanzhen.b.service.pojo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 17:48
 * Description:
 * Version: V1.0
 */
@Data
public class SchoolInstitutionDTO {

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private String code;

    private String name;

    private String institutionTradingName;

    private String institutionName;

    private String institutionType;

    private String institutionPostalAddress;

    private String website;

    private String summary;

    private boolean isFreeze;

    private List<SchoolCourseDTO> schoolCourseDTOS;

    private List<SchoolInstitutionLocationDTO> schoolInstitutionLocationDTOS;
}
