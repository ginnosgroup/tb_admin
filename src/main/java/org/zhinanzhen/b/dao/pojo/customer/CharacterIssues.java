package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CharacterIssues {

    private int allFalse;
    private List<PoliceClearance> cl1;
    private List<Detention> cl2;
    private List<Template> cl3;
    private List<Template> cl4;
    private List<Template> cl5;
    private List<Template> cl6;
    private List<Template> cl7;
    private List<Template> cl8;
    private List<Template> cl9;
    private List<Template> cl10;
    private List<Template> cl11;
    private List<Template> cl12;
    private List<Template> cl13;
    private List<Template> cl14;
    private List<Template> cl15;
    private List<Template> cl16;
    private List<Template> cl17;
    private List<Template> cl18;
    private List<Template> cl19;
    private List<Template> cl20;
    private List<Template> cl21;
    private List<Template> cl22;
    private List<Template> cl23;
    private List<Template> cl24;
    private List<Military> cl25;
    private List<MilitaryForce> cl26;
    private List<Template> cl27;
}
@Data
class PoliceClearance{
    private String name;

    private String country;

    private Date applicationDate;

    private Date dateOfIssue;

    private String referenceNumber;
}
@Data
class Detention{
    private String name;

    private Date dateForm;

    private Date dateTo;

    private String country;

    private String details;
}

@Data
class Template {
    private String name;

    private String country;

    private Date date;

    private String type;

    private String details;
}
@Data
class Military{
    private String name;

    private Date dateForm;

    private Date dateTo;

    private String country;

    private String type;

    private String details;


}
@Data
class MilitaryForce{
    private String name;

    private Date dateForm;

    private Date dateTo;

    private String country;

    private String type;

    private String nameOfOrganisation;

    private String position;

    private String countryOfDeployment;

    private String Duties;
}
