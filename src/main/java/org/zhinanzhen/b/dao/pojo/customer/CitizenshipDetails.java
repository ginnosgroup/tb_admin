package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;

@Data
public class CitizenshipDetails {
    private int isAnyCountry;

    private String notAnyCountryInformation;

    private List<Citizenship> citizenshipList;//公民身份详情列表

}

@Data
 class Citizenship {

    private String countryOfCitizenship;

    private String obtained;

    private String obtainedDate;

    private int isHold;

    private String ceasedDate;

    private String ceaseReason;

    private int isHeldPassport;

    private List<PassportsOrTravelDocuments> list;
}
