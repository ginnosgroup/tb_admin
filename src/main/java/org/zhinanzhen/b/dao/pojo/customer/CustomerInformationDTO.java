package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.Date;

@Data
public class CustomerInformationDTO {
    private Integer id;

    private Date gmtCreate;

    private Date gmtModify;

    private Integer serviceOrderId;//服务订单编号

    private String mainInformation;//基本信息

    private String pastInformationList;//曾用信息列表

    private String chineseCommercialCode;//中文电码

    private String contactDetails;//联系信息列表

    private String citizenshipDetails;//公民身份详情

    private String identificationNumberList;//护照,旅行证件详细信息

    private String educationList;//教育信息

    private String languageSkills;//语言能力

    private String addresses;//地址信息

    private String postalAddressList;//邮件地址列表

    private String healthQuestions;//健康信息

    private String characterIssues;//性格信息
}