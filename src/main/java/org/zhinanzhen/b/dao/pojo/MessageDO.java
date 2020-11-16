package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class MessageDO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;
	
	private Date gmtCreate;
	
	private int adminUserId;
	
	private int knowledgeId;
	
	private String content;

}
