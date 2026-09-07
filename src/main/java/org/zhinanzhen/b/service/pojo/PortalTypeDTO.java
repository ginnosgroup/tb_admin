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

	/** 该案件类型对应的客户材料清单文件路径。 */
	private String filePath;

	private int sort;

	private Integer isDelete;

}
