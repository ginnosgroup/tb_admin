package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class MaraDO {

	private int id;

	private Date gmtCreate;

	private String name;

	private String phone;

	private String email;

	private String state;

	private String imageUrl;

	private int regionId;

}
