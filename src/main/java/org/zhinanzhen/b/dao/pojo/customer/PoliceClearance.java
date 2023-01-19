package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.Date;

@Data
public class PoliceClearance {
    private String name;

    private String country;

    private Date applicationDate;

    private Date dateOfIssue;

    private String referenceNumber;
}
