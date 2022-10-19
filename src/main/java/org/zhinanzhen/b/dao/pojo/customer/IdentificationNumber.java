package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

@Data
public class IdentificationNumber {

    private String type;

    private String identificationNumber;

    private String familyName;

    private String givenName;

    private String issueDate;

    private String expiryDate;

    private String CountryOfIssue;
}
