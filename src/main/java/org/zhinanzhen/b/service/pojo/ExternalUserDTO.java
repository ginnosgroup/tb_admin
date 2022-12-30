package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ExternalUserDTO {

	// follow_info

	private String userId;

	private String remark;

	private String description;

	private Date createtime;
	
	private List<String> tagId;
	
	private List<String> remarkMobiles;

	private int addWay;

	private String operUserid;

	// external_contact

	private String externalUserid;

	private String name;
	
	private int type;
	
	private String avatar;
	
	private String gender;
	
	private String unionid;

}
