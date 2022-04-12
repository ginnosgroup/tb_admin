package org.zhinanzhen.b.service.pojo;

import lombok.Data;

@Data
public class ServicePackagePriceDTO {

	private int id;

	private double minPrice;

	private double maxPrice;
	
	private Integer serviceId;

	private int regionId;
	
	private String regionName;

}
