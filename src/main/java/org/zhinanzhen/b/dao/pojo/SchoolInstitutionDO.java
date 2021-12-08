package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 16:21
 * Description:
 * Version: V1.0
 */
@Data
public class SchoolInstitutionDO implements Serializable {

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
}
