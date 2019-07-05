package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class MessageTopicDTO {

	private int id;

	private Date gmtCreate;

	private int userId;

	private String title;

	private String content;

}
