package org.zhinanzhen.b.service.pojo;

import lombok.Data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 17:53
 * Description:
 * Version: V1.0
 */
@Data
public class SchoolInstitutionLocationDTO {

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private String name;

    private String state;

    private int numberOfCourses;

    private int providerId;

    private String providerCode;

    private String phone;
}
