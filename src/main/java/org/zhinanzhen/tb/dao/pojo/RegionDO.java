package org.zhinanzhen.tb.dao.pojo;

import lombok.Data;

@Data
public class RegionDO {
	
	private int id;
	
	private String name;
	
	private Integer parentId;
	
	private int weight;

}
