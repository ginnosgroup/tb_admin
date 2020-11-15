package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class ServiceOrderOfficialRemarksDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private int officialId;

	private int serviceOrderId;
	
	private String content;

}
