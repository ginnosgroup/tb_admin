package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class PortalDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private int typeId;

	/** 案件类型标识。 */
	private String caseType;

	private String name;

	private String gender;

	private Date birthday;

	private String passport;

	private String englishScore;

	private Date completionDate;

	private Date visaExpirationDate;

	private Date examResultsDate;

	private Date studentVisaExpirationDate;

	private Boolean hasCompletionLetter;

	private String jsonStr;

	/** 顾问、MARA填写的合同表单JSON数据。 */
	private String contractStr;

	/** 生成后的合同文件访问路径，例如 /uploads/portal_document/xxx.pdf。 */
	private String contractFilePath;

	/** 生成后的Letter文件访问路径，例如 /uploads/portal_document/xxx.docx。 */
	private String letterFilePath;

	private String aiConsultContent;

	private int adviserId;

	private int officialId;

	private int maraId;

	private int serviceOrderId;

	private String strState;

}
