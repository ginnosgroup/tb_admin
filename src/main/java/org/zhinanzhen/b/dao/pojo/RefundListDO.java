package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class RefundListDO extends RefundDO {
	
	@Getter
	@Setter
	private String officialName;

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
