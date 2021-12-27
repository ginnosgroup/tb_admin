package org.zhinanzhen.b.service.pojo;

import org.zhinanzhen.b.dao.pojo.RefundDO;

import lombok.Getter;
import lombok.Setter;

public class RefundDTO extends RefundDO {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String schoolName;

	@Getter
	@Setter
	private String institutionName;

	@Getter
	@Setter
	private String courseName;

}
