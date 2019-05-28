package org.zhinanzhen.b.service.pojo;

import org.zhinanzhen.b.service.AbleStateEnum;

import lombok.Data;

@Data
public class ReceiveTypeDTO {

	private int id;

	private String name;

	private AbleStateEnum state;

	private int weight;
	
}
