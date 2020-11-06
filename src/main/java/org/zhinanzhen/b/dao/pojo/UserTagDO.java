package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserTagDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private int userId;

	private int tagId;

}
