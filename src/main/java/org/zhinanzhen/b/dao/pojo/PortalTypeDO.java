package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class PortalTypeDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private String name;

	private String description;

	/** 该案件类型对应的客户材料清单文件路径。 */
	private String filePath;

	/** 该案件类型对应的材料清单JSON字符串。 */
	private String documentList;

	private int sort;

	private Integer isDelete;

}
