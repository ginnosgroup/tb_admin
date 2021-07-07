package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import lombok.Getter;
import lombok.Setter;

public class CommissionOrderListDTO extends CommissionOrderDTO {

	@Getter
	@Setter
	private Date signingDate;

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
	private Date birthday;

	@Getter
	@Setter
	private SchoolDTO school;

	@Getter
	@Setter
	private AdviserDTO adviser;

	@Getter
	@Setter
	private ReceiveTypeDTO receiveType;

	@Getter
	@Setter
	private int serviceId;

	@Getter
	@Setter
	private ServiceDTO service;

	@Getter
	@Setter
	private double totalPerAmount;

	@Getter
	@Setter
	private double totalAmount;
	
	@Getter
	@Setter
	private String refuseReason;

	@Getter
	@Setter
	private List<MailRemindDTO> mailRemindDTOS;

}
