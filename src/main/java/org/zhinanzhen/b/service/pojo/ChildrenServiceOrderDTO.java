package org.zhinanzhen.b.service.pojo;

import lombok.Data;

@Data
public class ChildrenServiceOrderDTO {
	
	private int id;

	private Integer recommendAdviserId;
	
	private String type;
	
	private int servicePackageId;
	
	private String servicePackageType;
	
	private String state;

}
