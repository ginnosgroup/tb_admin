package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;

@Data
public class Addresses {

    private int isAll;

    private List<CurrentAddress> currentAddressList;

    private int isSamePostalAddress;

    private int isSameResidential;

}

@Data
class CurrentAddress {

    private String country;

    private String addresses;

    private String streetLine2;

    private String suburb;

    private String state;

    private String postCode;

    private List<Applicant> applicantList;

}
@Data
class Applicant {

    private String names;

    private String dataFrom;

    private String dataTo;

    private String legalStatus;
}
