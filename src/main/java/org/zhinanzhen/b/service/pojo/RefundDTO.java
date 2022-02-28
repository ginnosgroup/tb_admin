package org.zhinanzhen.b.service.pojo;

import org.zhinanzhen.b.dao.pojo.RefundDO;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
	private String institutionTradingName;

	@Getter
	@Setter
	private String courseName;
	
	@Getter
	@Setter
	private String maraName;

	@Getter
	@Setter
	private String serviceName;

	@Getter
	@Setter
	private Date kjApprovalDate;

}
