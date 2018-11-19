package org.zhinanzhen.tb.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SubjectDO {

	private int id;
	
	private Date gmtCreate;

	private String name;
	
	private String type;
	
	private int parentId;
	
	private String logo;

	private double price;

	private Date startDate;

	private Date endDate;

	private String state;

	private int categoryId;

	private double preAmount;

	private String codex;

	private String details;

	private String regionIds;

	private int weight;

}
