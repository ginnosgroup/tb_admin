package org.zhinanzhen.tb.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class UserAdviserDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date gmtCreate;

	private int userId;

	private int adviserId;
	
	private boolean isCreater;

}
