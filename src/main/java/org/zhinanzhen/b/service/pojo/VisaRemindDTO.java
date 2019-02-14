package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class VisaRemindDTO {

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private int visaId;

	private Date remindDate;

	private VisaDTO visa;

}
