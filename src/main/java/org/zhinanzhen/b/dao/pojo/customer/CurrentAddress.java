package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;

@Data
public class CurrentAddress {

    private String country;

    private String addresses;

    private String streetLine2;

    private String suburb;

    private String state;

    private String postCode;

    private List<Applicant> applicantList;

}
