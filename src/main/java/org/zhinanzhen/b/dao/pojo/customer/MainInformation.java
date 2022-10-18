package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

@Data
public class MainInformation {

    private String prefixTitle;//称呼

    private int gender;//性别

    private String givenName;//名字拼音

    private String familyName;//姓拼音

    private String preferredNames;//首选名称

    private String dateOfBirth;//出生日期

    private String birthLocation;//出生地点

    private String birthCountry;//出生国家

    private String StateOrProvince;//省份

    private String maritalStatus;//婚姻状态

    private int isHasNewRelationship;//新关系

    private int isLive;//保持关系/订婚

}
