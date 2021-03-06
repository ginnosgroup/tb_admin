package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class KnowledgeDTO {

	private int id;

	private Date gmtCreate;

	private String title;

	private String content;

	private String password;

	private int knowledgeMenuId;

	private String knowledgeMenuName;

	private int knowledgeMenuId2;

	private String knowledgeMenuName2;

	private int adminUserId;

	private String adminUserName;

}
