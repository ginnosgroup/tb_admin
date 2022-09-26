package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class OfficialGradeDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private Date gmt_create;

    private Date gmt_modify;

    private String grade;

    private Double rate;

    private int ruler;

}
