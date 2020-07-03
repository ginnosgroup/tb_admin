package org.zhinanzhen.b.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BatchUpdateVisa {

	private int id;

	private Double sureExpectAmount;

	private Double bonus;

	private String bonusDate;

}
