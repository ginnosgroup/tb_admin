package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;

@Data
public class Citizenship {

    private String countryOfCitizenship;

    private String obtained;

    private String obtainedDate;

    private int isHold;

    private String ceasedDate;

    private String ceaseReason;

    private int isHeldPassport;

    private List<PassportsOrTravelDocuments> list;
}
