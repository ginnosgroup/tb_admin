package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class KnowledgeDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date gmtCreate;

	private String title;

	private String content;
	
	private String password;

	private int knowledgeMenuId;
	
	private int adminUserId;

}
