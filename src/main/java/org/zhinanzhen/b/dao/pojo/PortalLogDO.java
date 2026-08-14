package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class PortalLogDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private int portalId;

	private Date gmtCreate;

	private Integer operatorId;

	private String operatorName;

	private String role;

	private String action;

	private String fromState;

	private String toState;

	private String content;

	private String ip;

	private String userAgent;

}
