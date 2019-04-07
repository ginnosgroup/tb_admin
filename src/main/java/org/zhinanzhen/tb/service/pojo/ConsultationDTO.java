package org.zhinanzhen.tb.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class ConsultationDTO {

	private Integer id;

	private Date gmtCreate;

	private Integer userId;
	
	private String userName;

	private String contents;

	private Date remindDate;

	private String remindContents;

}
