package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class ServiceOrderOfficialRemarksDO {

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private int officialId;

	private int serviceOrderId;

}
