package org.zhinanzhen.b.service.pojo;

import lombok.Data;

@Data
public class ServiceOrderReviewDTO {

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
