package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class SchoolInstitutionCountDO implements Serializable {
	
	private String courseName;

    private String name;

    private int count;

}
