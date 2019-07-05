package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class MessageDTO {

	private int id;

	private Date gmtCreate;

	private int userId;

	private int topicId;

	private String content;

	private int parentId;

}
