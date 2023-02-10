package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

@Data
public class PoliceClearance {
    private String name;

    private String country;

    private String applicationDate;

    private String dateOfIssue;

    private String referenceNumber;
}
