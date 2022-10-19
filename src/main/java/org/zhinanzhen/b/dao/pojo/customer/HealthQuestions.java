package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class HealthQuestions {
    private int allFalse;
    private List<HealthExamination> lista;
    private List<HealthTemplate> listb;
    private List<HealthTemplate> listc;
    private List<HealthTemplate> listd;
    private List<HealthTemplate> liste;
    private List<HealthTemplate> listf;
    private List<HealthTemplate> listg;
    private List<HealthTemplate> listh1;
    private List<HealthTemplate> listh2;
    private List<HealthTemplate> listh3;
    private List<HealthTemplate> listh4;
    private List<HealthTemplate> listh5;
    private List<HealthTemplate> listh6;
    private List<HealthTemplate> listh7;
    private List<HealthTemplate> listh8;
    private List<HealthTemplate> listh9;
    private List<HealthTemplate> listh10;
    private List<HealthTemplate> listh11;
    private List<HealthTemplate> listh12;
    private List<HealthTemplate> listh13;
    private List<HealthTemplate> listi;
    private List<HealthTemplate> listj;

}

@Data
class HealthExamination {

    private String name;

    private Date date;

    private String country;

    private int hapId;
}

@Data
class HealthTemplate {

    private String name;

    private String reason;

    private String details;
}
