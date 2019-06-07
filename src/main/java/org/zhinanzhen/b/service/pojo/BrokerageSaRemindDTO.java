package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class BrokerageSaRemindDTO {

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private int brokerageSaId;

	private Date remindDate;
	
	private String state;

	private BrokerageSaDTO brokerageSa;

}
