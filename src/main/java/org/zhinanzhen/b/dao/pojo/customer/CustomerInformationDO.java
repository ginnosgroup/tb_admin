package org.zhinanzhen.b.dao.pojo.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInformationDO {
    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private Integer serviceOrderId;//服务订单编号

    private Integer applicantId;//申请人id

    private MainInformation mainInformation;//基本信息

    private List<PastInformation> pastInformationList;//曾用信息列表

    private String chineseCommercialCode;//中文电码

    private ContactDetails contactDetails;//联系信息列表

    private CitizenshipDetails citizenshipDetails;//公民身份详情

    private List<IdentificationNumber> identificationNumberList;//护照,旅行证件详细信息

    private List<Education> educationList;//教育信息

    private List<LanguageSkills> languageSkills;//语言能力

    private Addresses addresses;//地址信息

    private List<PostalAddress> postalAddressList;//邮件地址列表

    private HealthQuestions healthQuestions;//健康信息

    private CharacterIssues characterIssues;//性格信息

    private Parents parents;//父母信息

    private Siblings siblings;//兄弟姐妹信息

    private Url url;//资料列表

    private String mmdiskPath;//MM存储目录
}