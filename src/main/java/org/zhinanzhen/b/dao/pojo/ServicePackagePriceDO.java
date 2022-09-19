package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class ServicePackagePriceDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private double minPrice;

    private double maxPrice;

    private Integer serviceId;

    private int regionId;

    private String regionName;

    private double costPrince;

    private double thirdPrince;

    private Integer ruler;

    private double amount;

}
