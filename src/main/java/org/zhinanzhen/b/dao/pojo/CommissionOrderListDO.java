package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class CommissionOrderListDO extends CommissionOrderDO {

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

}
