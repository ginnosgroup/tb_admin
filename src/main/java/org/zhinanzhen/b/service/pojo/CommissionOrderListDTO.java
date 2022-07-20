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
	private OfficialDTO official;

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
	private double totalPerAmount; // 总计应收
	
	@Getter
	@Setter
	private double totalPerAmountAUD; // 总计应收(澳币)
	
	@Getter
	@Setter
	private double totalPerAmountCNY; // 总计应收(人民币)

	@Getter
	@Setter
	private double totalAmount; // 总计收款
	
	@Getter
	@Setter
	private double totalAmountAUD; // 总计收款(澳币)
	
	@Getter
	@Setter
	private double totalAmountCNY; // 总计收款(人民币)
	
	@Getter
	@Setter
	private String refuseReason;

	@Getter
	@Setter
	private List<MailRemindDTO> mailRemindDTOS;

	@Getter
	@Setter
	private SchoolInstitutionListDTO schoolInstitutionListDTO;

	/*
	 * 记录培训学校的名字
	 */
	@Getter
	@Setter
	private String institutionTradingName;

	@Getter
	@Setter
	private boolean isRefunded = false;

}
