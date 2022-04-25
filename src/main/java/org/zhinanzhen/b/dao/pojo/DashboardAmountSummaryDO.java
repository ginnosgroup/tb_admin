package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class DashboardAmountSummaryDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private String name1;

	private String name2;

	private int total;

	private double amount;

}
