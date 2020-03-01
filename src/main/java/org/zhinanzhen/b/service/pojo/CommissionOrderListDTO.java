package org.zhinanzhen.b.service.pojo;

import org.zhinanzhen.tb.service.pojo.UserDTO;

import lombok.Getter;
import lombok.Setter;

public class CommissionOrderListDTO extends CommissionOrderDTO {

	@Getter
	@Setter
	private Double amount;

	@Getter
	@Setter
	private Double expectAmount;

	@Getter
	@Setter
	private UserDTO user;

	@Getter
	@Setter
	private SchoolDTO school;

}
