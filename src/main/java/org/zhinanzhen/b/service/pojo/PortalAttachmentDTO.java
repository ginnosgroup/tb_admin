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

	/** 附件业务类型（passport/coe/completion/autoCourse/autoLanguage），其余场景为空。 */
	private String attachmentType;

	/** 文件排序；同组申请材料可带 -1、-2 等顺序后缀。 */
	private String fileSorting;

	private String stage;

	/** 附件状态。 */
	private String attachmentState;

	private String aiText;

	private String ip;

	private String userAgent;
}
