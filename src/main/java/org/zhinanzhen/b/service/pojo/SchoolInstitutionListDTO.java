package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.SchoolCourseDO;
import org.zhinanzhen.b.dao.pojo.SchoolInstitutionLocationDO;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/10/25 下午 2:14
 * Description:
 * Version: V1.0
 */
@Data
public class SchoolInstitutionListDTO {
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

    private SchoolCourseDO schoolCourseDO;

    private SchoolInstitutionLocationDO schoolInstitutionLocationDO;
}
