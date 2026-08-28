package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class PortalLogDTO {

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
