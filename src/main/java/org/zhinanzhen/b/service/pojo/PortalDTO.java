package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class PortalDTO {

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private int typeId;

	private String portalTypeName;

	/** 关联的案件类型对象（按 typeId 查询组装）。 */
	private PortalTypeDTO portalType;

	private String name;

	private String gender;

	private Date birthday;

	private String passport;

	private String englishScore;

	private Date completionDate;

	private Date visaExpirationDate;

	private Date examResultsDate;

	private Date studentVisaExpirationDate;

	private Boolean hasCompletionLetter;

	private String jsonStr;

	private int adviserId;

	private int officialId;

	private int maraId;

	private String adviserName;

	private String officialName;

	private String maraName;

	private int serviceOrderId;

	private String strState;

	/** 案件关联的附件列表（按 portal_id 查询组装）。 */
	private List<PortalAttachmentDTO> portalAttachmentList;

}
