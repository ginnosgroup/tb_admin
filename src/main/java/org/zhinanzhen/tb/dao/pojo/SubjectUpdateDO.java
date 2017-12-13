package org.zhinanzhen.tb.dao.pojo;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

public class SubjectUpdateDO extends SubjectDO {

	@Getter
	@Setter
	public Timestamp startDateTimpstamp;

	@Getter
	@Setter
	public Timestamp endDateTimpstamp;

}
