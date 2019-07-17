package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class MessageDTO {

	private int id;

	private Date gmtCreate;

	private int adminUserId;

	private int knowledgeId;

	private String content;

}
