package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class OrderMaraAnnotationDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private int serviceOrderId;

    private String maraMark;

    private Boolean isCheck;

    private Date checkTime;

    private Integer officialId;

}
