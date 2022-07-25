package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class EverydayExchangeRateDO {

	private int id;

	private String currency;

	private double originalExchangeRate;

	private double znzExchangeRate;

	private Date updateTime;

}
