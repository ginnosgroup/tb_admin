package org.zhinanzhen.b.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class BrokerageDO {

	private int id;

	private Date gmtCreate;

	private int userId;

	private int receiveTypeId;

	private Date receiveDate;

	private int serviceId;

	private double receivable;

	private double received;

	private double amount;

	private double gst;

	private double deductGst;

	private double bonus;

	private double adviserId;

	private double officialId;

}
