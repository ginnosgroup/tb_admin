package org.zhinanzhen.tb.service.pojo;

import lombok.Data;

@Data
public class SubjectPriceIntervalDTO {
	
	private int id;
	
	private int subjectId;
	
	private Integer startNum;
	
	private Integer endNum;
	
	private String regionIds;

}
