package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Date: 2022/02/12 下午 11:00
 * Description:
 * Version: V1.0
 */
@Data
public class RefoundReportDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String date;

    private int regionId;

    private String area;

    private int adviserId;

    private String consultant;

    private double refunded;

    private double refunding;
}
