package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class PortalSupplementDTO {

	private int id;
	private int portalId;
	private int roundNo;
	private String status;
	private Integer officialId;
	private Date requestedAt;
	private Date customerSubmittedAt;
	private Date submittedToMaraAt;
	private Integer maraId;
	private Date reviewedAt;
	private String reviewRemark;
	/** 本轮附件路径，多个路径使用逗号分隔；仅查询返回，不落库。 */
	private String filePath;
	/** 关联该补料轮次的案件操作日志。 */
	private List<PortalLogDTO> portalLogList;
	private Date gmtCreate;
	private Date gmtModify;
}
