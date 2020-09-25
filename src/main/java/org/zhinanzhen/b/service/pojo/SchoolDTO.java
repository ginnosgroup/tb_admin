package org.zhinanzhen.b.service.pojo;

import lombok.Data;

@Data
public class SchoolDTO {

	private int id;

	private String name;

	private String subject;

	private String country;

	private SchoolAttachmentsDTO schoolAttachments;

}
