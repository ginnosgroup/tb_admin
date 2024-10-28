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

	private boolean isLongTime; // 判断是否为长期签证

	private List<ServicePackagePriceDTO> ServicePackagePirceList;
}
