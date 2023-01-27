package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.Date;

@Data
public class MilitaryForce {
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
