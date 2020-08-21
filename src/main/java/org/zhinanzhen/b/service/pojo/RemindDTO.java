package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class RemindDTO {

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private int serviceOrderId;

	private Date remindDate;

	private String state;

	private ServiceOrderDTO serviceOrderDto;

}
