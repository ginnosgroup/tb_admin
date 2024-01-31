package org.zhinanzhen.b.service.pojo;

import java.util.List;

import lombok.Data;

@Data
public class ServiceDTO {

	private int id;

	private String name;

	private String code;

	private String role;

	private boolean isZx;
	
	private boolean isDelete;

	private List<ServicePackagePriceDTO> ServicePackagePirceList;
}
