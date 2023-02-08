package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.Date;

@Data
public class HealthExamination {

    private String name;

    private String date;

    private String country;

    private int hapId;
}
