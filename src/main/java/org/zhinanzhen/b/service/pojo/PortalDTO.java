package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
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

	/** 顾问、MARA填写的合同表单JSON数据。 */
	private String contractStr;

	/** 语聚AI返回的485方案咨询内容（updatePortal时保存）。 */
	private String aiConsultContent;

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

	/** 案件操作日志列表（按 portal_id 查询组装）。 */
	private List<PortalLogDTO> portalLogList;

	/** 仅在更新案件并调用语聚AI时返回，不参与数据库持久化。 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Map<String, Object> yujuAiResult;

	/** 状态转为03时生成的合同和建议信路径，不参与数据库持久化。 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Map<String, String> generatedDocumentPaths;

}
