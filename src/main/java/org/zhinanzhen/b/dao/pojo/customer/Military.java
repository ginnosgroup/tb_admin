package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.Date;

@Data
public class Military {
    private String name;

    private Date dateForm;

    private Date dateTo;

    private String country;

    private String type;

    private String details;


}
