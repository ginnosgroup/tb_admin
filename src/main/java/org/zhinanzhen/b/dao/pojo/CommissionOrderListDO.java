package org.zhinanzhen.b.dao.pojo;

import lombok.Getter;
import lombok.Setter;

public class CommissionOrderListDO extends CommissionOrderDO {

	@Getter
	@Setter
	private Integer userId;

	@Getter
	@Setter
	private Integer schoolId;

}
