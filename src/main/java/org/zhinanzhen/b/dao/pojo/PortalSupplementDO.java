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
	/** 仅用于状态更新时通知Mapper清空后续节点时间，不映射数据库列。 */
	private boolean clearSubsequentTimestamps;
	private Date gmtCreate;
	private Date gmtModify;
}
