package org.zhinanzhen.tb.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class AdviserDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date gmtCreate;

	private String name;

	private String phone;

	private String email;

	private String state;

	private String imageUrl;

	private int regionId;

	private String operUserId;

}
