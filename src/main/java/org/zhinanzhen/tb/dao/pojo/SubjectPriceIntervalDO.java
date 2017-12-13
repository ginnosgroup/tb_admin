package org.zhinanzhen.tb.dao.pojo;

import lombok.Data;

@Data
public class SubjectPriceIntervalDO {

	private int id;

	private int subjectId;

	private Integer startNum;

	private Integer endNum;

	private String regionIds;

}
