package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class SchoolBrokerageSaListDO extends SchoolBrokerageSaDO {
	
	@Getter
	@Setter
	private String subagencyName;

	@Getter
	@Setter
	private String schoolName;

	@Getter
	@Setter
	private String schoolSubject;

	@Getter
	@Setter
	private String userName;
	
	@Getter
	@Setter
	private Date birthday;

	@Getter
	@Setter
	private String phone;

}
