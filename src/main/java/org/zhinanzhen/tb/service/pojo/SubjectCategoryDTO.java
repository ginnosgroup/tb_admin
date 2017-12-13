package org.zhinanzhen.tb.service.pojo;

import org.zhinanzhen.tb.service.SubjectCategoryStateEnum;

import lombok.Data;

@Data
public class SubjectCategoryDTO {

	private int id;

	private String name;

	private SubjectCategoryStateEnum state;

	private int weight;

}
