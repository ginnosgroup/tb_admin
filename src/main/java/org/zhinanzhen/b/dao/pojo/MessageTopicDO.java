package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class MessageTopicDO {
	
	private int id;
	
	private Date gmtCreate;
	
	private int userId;
	
	private String title;
	
	private String content;

}
