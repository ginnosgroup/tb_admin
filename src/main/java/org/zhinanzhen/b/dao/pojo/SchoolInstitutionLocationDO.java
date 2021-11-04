package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 16:35
 * Description:
 * Version: V1.0
 */
@Data
public class SchoolInstitutionLocationDO {

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private String name;

    private String state;

    private int numberOfCourses;

    private int providerId;

    private String providerCode;

    private String phone;

    private String address;
}
