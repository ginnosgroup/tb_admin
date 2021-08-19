package org.zhinanzhen.b.service.pojo;

import lombok.Data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 17:52
 * Description:
 * Version: V1.0
 */
@Data
public class SchoolCourseDTO {

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private String courseName;

    private String courseSector;

    private String courseLevel;

    private int providerId;

    private String providerCode;

    private String courseCode;

    private boolean isFreeze;
}
