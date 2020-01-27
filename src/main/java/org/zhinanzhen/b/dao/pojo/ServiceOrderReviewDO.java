package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

@Data
public class ServiceOrderReviewDO {

	private int id;
	
	private int serviceOrderId;
	
	private String state;
	
	private String type;
	
	private int adminUserId;

}
