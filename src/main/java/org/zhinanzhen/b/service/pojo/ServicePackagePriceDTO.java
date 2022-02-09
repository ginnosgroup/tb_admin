package org.zhinanzhen.b.service.pojo;

import lombok.Data;

@Data
public class ServicePackagePriceDTO {

	private int id;

	private double minPrice;

	private double maxPrice;

	private int servicePackageId;

	private int regionId;

}
