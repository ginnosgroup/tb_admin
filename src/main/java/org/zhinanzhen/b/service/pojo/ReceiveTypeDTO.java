package org.zhinanzhen.b.service.pojo;

import org.zhinanzhen.b.service.ReceiveTypeStateEnum;

import lombok.Data;

@Data
public class ReceiveTypeDTO {

	private int id;

	private String name;

	private ReceiveTypeStateEnum state;

	private int weight;
	
}
