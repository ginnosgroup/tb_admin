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

	/** 案件类型标识。 */
	private String caseType;

	private String portalTypeName;

	/** 关联的案件类型对象（按 typeId 查询组装）。 */
	private PortalTypeDTO portalType;

	private String name;

	private String gender;

	private Date birthday;

	private String passport;

	private String jsonStr;

	/** 顾问、MARA填写的合同表单JSON数据。 */
	private String contractStr;

	/** 生成后的合同文件访问路径，例如 /uploads/portal_document/xxx.pdf。 */
	private String contractFilePath;

	/** 生成后的Letter文件访问路径，例如 /uploads/portal_document/xxx.docx。 */
	private String letterFilePath;

	/** 生成后的 Form 956 文件访问路径，对应 b_portal_list.956path。 */
	private String form956Path;

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

	/** stage=application 的申请文件路径，多个路径使用逗号分隔。 */
	private String applicationFilePath;

	/** stage=applicationWA 的申请文件路径，多个路径使用逗号分隔。 */
	private String applicationWAFilePath;

	/** 案件操作日志列表（按 portal_id 查询组装）。 */
	private List<PortalLogDTO> portalLogList;

	/** 仅在更新案件并调用语聚AI时返回，不参与数据库持久化。 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Map<String, Object> yujuAiResult;

	/** 状态转为02B时本次生成的合同、建议信和 Form 956 路径。 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Map<String, String> generatedDocumentPaths;

}
