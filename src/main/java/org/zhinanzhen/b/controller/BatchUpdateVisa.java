package org.zhinanzhen.b.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BatchUpdateVisa {

	public BatchUpdateVisa() {
	}

	private int id;

	private Double sureExpectAmount;

	private Double bonus;

	private String bonusDate;

}
