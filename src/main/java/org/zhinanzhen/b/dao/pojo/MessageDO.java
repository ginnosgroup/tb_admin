package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class MessageDO {
	
	private int id;
	
	private Date gmtCreate;
	
	private int adminUserId;
	
	private int knowledgeId;
	
	private String content;

}
