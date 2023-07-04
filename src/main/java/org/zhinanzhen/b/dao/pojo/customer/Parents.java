package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;

@Data
public class Parents {
    private int isKnow;

    private String details;

    private List<ParentsInformation> parentsInformationList;
}
