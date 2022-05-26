package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class VisaListDO extends VisaDO {
	
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String userName;

	@Getter
	@Setter
	private Date birthday;

	@Getter
	@Setter
	private String phone;
	
	@Getter
	@Setter
	private int applicantId;

}
