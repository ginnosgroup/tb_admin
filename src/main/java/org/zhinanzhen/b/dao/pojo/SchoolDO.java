package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class SchoolDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private String name;

	private String subject;

	private String country;

}
