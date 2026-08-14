package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class PortalAttachmentDTO {

	private int id;

	private Integer portalId;

	private Date gmtCreate;

	private Date gmtModify;

	private String fileName;

	private String filePath;

	private Long fileSize;

	private String fileType;

	private String fileExt;

	private String stage;

}
