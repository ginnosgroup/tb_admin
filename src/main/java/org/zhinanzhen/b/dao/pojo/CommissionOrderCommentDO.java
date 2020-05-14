package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class CommissionOrderCommentDO {

	private int id;

	private Date gmtCreate;

	private int adminUserId;

	private int commissionOrderId;

	private String content;

}
