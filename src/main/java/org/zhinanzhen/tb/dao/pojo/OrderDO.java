package org.zhinanzhen.tb.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class OrderDO {

	private int id;

	private Date gmtCreate;

	private String name;

	private String state;

	private int subjectId;

	private int num;

	private String payType;

	private double amount;

	private String payCode;

	private double payAmount;

	private Date payDate;

	private double createPrice;

	private double finishPrice;

	private int userId;

	private int introducerUserId;

	private int adviserId;

	private Date adviserDate;

	private int regionId;

	private double remainPayAmount;

	private Date remainPayDate;

	private double remainPayBalance;

}
