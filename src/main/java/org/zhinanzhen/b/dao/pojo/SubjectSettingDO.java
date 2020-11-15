package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;;

@Data
@ToString
public class SubjectSettingDO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;
	
	private int schoolSettingId;
	
	private String subject;
	
	private double price;

}
