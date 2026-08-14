package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class PortalDTO {

	private int id;

	private Date gmtCreate;

	private Date gmtModify;

	private int typeId;

	private String portalTypeName;

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

	private int adviserId;

	private int officialId;

	private int maraId;

	private String adviserName;

	private String officialName;

	private String maraName;

	private int serviceOrderId;

	private String strState;

}
