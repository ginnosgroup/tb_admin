package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

@Data
public class ParentsInformation {

    private String givenName;//父母名字拼音

    private String familyName;//父母姓拼音

    private String relationship;//关系

    private String gender;//性别

    private String dateOfBirth;//出生日期

    private String birthLocation;//出生地点

    private String birthCountry;//出生国家

    private String StateOrProvince;//省份

}
