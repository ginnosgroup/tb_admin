package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class BrokerageDTO {

	private int id;

	private Date gmtCreate;

	private Date handlingDate;

	private int userId;

	private String userName;

	private Date birthday;

	private String phone;

	private int receiveTypeId;

	private Date receiveDate;

	private int serviceId;

	private double receivable;

	private double received;

	private double amount;

	private double gst;

	private double deductGst;

	private double bonus;

	private int adviserId;

	private int officialId;

	private boolean isClose;

}
