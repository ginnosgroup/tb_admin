package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class KnowledgeDO {

	private int id;

	private Date gmtCreate;

	private String title;

	private String content;
	
	private String password;

	private int knowledgeMenuId;

}
