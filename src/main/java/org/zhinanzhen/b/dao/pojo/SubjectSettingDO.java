package org.zhinanzhen.b.dao.pojo;

import lombok.Data;
import lombok.ToString;;

@Data
@ToString
public class SubjectSettingDO {
	
	private int id;
	
	private int schoolSettingId;
	
	private String subject;
	
	private double price;

}
