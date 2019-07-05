package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class MessageDO {
	
	private int id;
	
	private Date gmtCreate;
	
	private int userId;
	
	private int topicId; 
	
	private String content;
	
	private int parentId;

}
