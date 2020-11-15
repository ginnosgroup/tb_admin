package org.zhinanzhen.tb.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class RegionDO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;
	
	private String name;
	
	private Integer parentId;
	
	private int weight;

}
