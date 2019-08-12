package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SchoolSettingDTO {

	private int id;

	private String schoolName;

	private int type = 0;

	private Date startDate;

	private Date endDate;

	private String parameters;

	private double amount = 0.00;

	private int count = 0;

}
