package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.Date;

@Data
public class Template {
    private String name;

    private String country;

    private Date date;

    private String type;

    private String details;
}
