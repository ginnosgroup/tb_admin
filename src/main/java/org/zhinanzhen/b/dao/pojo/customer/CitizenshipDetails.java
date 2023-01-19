package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;

@Data
public class CitizenshipDetails {
    private int isAnyCountry;

    private String notAnyCountryInformation;

    private List<Citizenship> citizenshipList;//公民身份详情列表

}

