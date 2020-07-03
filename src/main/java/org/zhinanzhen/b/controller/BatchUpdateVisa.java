package org.zhinanzhen.b.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BatchUpdateVisa {

	private int id;

	private Double sureExpectAmount;

	private Double bonus;

	private String bonusDate;

}
