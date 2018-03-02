package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class RemindDO {

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private int schoolBrokerageSaId;
	
	private Date remindDate;

}
