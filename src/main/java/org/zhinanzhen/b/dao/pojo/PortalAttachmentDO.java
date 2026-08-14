package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class PortalAttachmentDO implements Serializable {

	private static final long serialVersionUID = 1L;

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
