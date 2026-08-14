package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class PortalTypeDTO {

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private String name;

	private String description;

	private int sort;

	private Integer isDelete;

}
