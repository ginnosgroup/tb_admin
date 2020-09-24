package org.zhinanzhen.b.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BatchUpdateCommissionOrder {

	private int id;

	private Double schoolPaymentAmount;

	private String schoolPaymentDate;

	private String invoiceNumber;

	private String zyDate;

	private Double sureExpectAmount;

	private Double bonus;

	private String bonusDate;

}