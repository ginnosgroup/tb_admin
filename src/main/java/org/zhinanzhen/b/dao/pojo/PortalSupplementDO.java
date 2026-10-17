package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class PortalSupplementDO implements Serializable {

	private static final long serialVersionUID = 1L;

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
	private Date reopentAt;
	private String reopenRemark;
	/** 仅用于状态更新时通知Mapper清空后续节点时间，不映射数据库列。 */
	private boolean clearSubsequentTimestamps;
	/** 仅用于客户提交补料时清空重新开放信息，不映射数据库列。 */
	private boolean clearReopenFields;
	private Date gmtCreate;
	private Date gmtModify;
}
