package org.zhinanzhen.b.service.pojo;

import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import lombok.Getter;
import lombok.Setter;

public class CommissionOrderListDTO extends CommissionOrderDTO {

	@Getter
	@Setter
	private int subagencyId;

	@Getter
	@Setter
	private SubagencyDTO subagency;

	@Getter
	@Setter
	private UserDTO user;

	@Getter
	@Setter
	private SchoolDTO school;

	@Getter
	@Setter
	private AdviserDTO adviser;

	@Getter
	@Setter
	private ReceiveTypeDTO receiveType;

}
