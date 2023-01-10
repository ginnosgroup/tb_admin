package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

@Data
public class PostalAddress {

    private String line1;

    private String line2;

    private String townOrCity;

    private int postCode;

    private String state;

    private String country;

    private String names;

    private String names2;


}
