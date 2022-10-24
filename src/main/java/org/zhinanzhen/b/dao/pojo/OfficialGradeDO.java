package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class OfficialGradeDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private String grade;

    private Double rate;

    private int ruler;

    private int flag;
}
