package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class CommissionOrderListDO extends CommissionOrderDO {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private int serviceOrderId;

	@Getter
	@Setter
	private Date signingDate;

	@Getter
	@Setter
	private int subagencyId;

	@Getter
	@Setter
	private int serviceId;

	@Getter
	@Setter
	private String institutionTradingName;
	
	@Getter
	@Setter
	private int applicantId;

}
