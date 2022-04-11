package org.zhinanzhen.b.service.pojo;

import java.util.List;

import lombok.Data;

@Data
public class ServicePackageDTO {

	private int id;

	private String type;

	private int serviceId;

	private String serviceCode;

	private String serviceName;

	private int num;

	private List<ServicePackagePriceDTO> ServicePackagePirceList;

}
