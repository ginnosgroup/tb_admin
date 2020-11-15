package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class ServiceOrderReviewDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private int serviceOrderId;
	
	private int commissionOrderId;

	private String adviserState;

	private String maraState;

	private String officialState;

	private String kjState;

	private String type;

	private int adminUserId;

}
