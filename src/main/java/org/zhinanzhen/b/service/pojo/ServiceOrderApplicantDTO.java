package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class ServiceOrderApplicantDTO {
	
	private int id;

	private Date gmtCreate;

	private Date gmtModify;
	
	private int serviceOrderId;
	
	private int applicantId;
	
	private String url;
	
	private String content;

}
