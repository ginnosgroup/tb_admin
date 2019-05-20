package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SourceDO {

	private int id;

	private Date gmtCreate;

	private String name;

	private int sourceRegionId;

}
