package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 16:01
 * Description:
 * Version: V1.0
 */
@Data
public class SchoolCourseDO {

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
