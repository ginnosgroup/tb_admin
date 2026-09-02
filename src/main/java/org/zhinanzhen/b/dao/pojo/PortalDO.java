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

	private String aiConsultContent;

	private int adviserId;

	private int officialId;

	private int maraId;

	private int serviceOrderId;

	private String strState;

}
