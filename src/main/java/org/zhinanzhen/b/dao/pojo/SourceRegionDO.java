package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SourceRegionDO {

	private int id;

	private Date gmtCreate;

	private String name;

	private int parentId;

}
