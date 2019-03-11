package org.zhinanzhen.tb.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class ConsultationDO {

	private Integer id;

	private Date gmtCreate;

	private Integer userId;

	private String contents;

	private Date remindDate;

	private String remindContents;

}
