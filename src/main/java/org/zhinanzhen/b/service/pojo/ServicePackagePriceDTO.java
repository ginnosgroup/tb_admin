package org.zhinanzhen.b.service.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ServicePackagePriceDTO {

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

	private String rulerV2;

	private List<ServicePackagePriceV2DTO> servicePackagePriceV2DTO;

}
