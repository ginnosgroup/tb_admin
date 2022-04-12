package org.zhinanzhen.b.service.pojo;

import lombok.Data;

@Data
public class ServicePackagePriceDTO {

	private int id;

	private double minPrice;

	private double maxPrice;

	private Integer servicePackageId;

	private int regionId;
	
	private String regionName;

}
