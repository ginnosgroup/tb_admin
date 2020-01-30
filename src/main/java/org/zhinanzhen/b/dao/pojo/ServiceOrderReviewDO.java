package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

@Data
public class ServiceOrderReviewDO {

	private int id;

	private int serviceOrderId;

	private String adviserState;

	private String maraState;

	private String officialState;

	private String kjState;

	private String type;

	private int adminUserId;

}
