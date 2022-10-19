package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

@Data
public class PassportsOrTravelDocuments {

    private String number;

    private int isShown;

    private String familyName;

    private String givenName;

    private int isListedOn;

    private String issueDate;

    private String expiryDate;

    private String Place;

    private String Status;
}
