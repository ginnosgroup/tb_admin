package org.zhinanzhen.tb.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class ConsultationDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private Date gmtCreate;

	private Integer userId;

	private String contents;
	
	private String state;

	private Date remindDate;

	private String remindContents;

}
