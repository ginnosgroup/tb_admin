package org.zhinanzhen.b.service.pojo;

import lombok.Data;

@Data
public class ServiceOrderReviewDTO {

	private int id;

	private int serviceOrderId;

	private String state;

	private String type;

	private int adminUserId;

}
