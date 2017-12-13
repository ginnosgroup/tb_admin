package org.zhinanzhen.tb.service.pojo;

import java.util.ArrayList;
import java.util.List;

import org.zhinanzhen.tb.dao.pojo.RegionDO;

import lombok.Data;

@Data
public class RegionDTO {

	private int id;

	private String name;

	private Integer parentId;
	
	private int weight;

	private List<RegionDO> regionList = new ArrayList<RegionDO>();
}
