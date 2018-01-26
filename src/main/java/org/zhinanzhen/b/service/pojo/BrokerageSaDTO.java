package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class BrokerageSaDTO {

	private int id;

	private Date gmtCreate;

	private Date handlingDate;

	private int userId;

	private int receiveTypeId;

	private int schoolId;

	private Date startDate;

	private Date endDate;

	private double tuitionFee;

	private double commission;

	private double gst;

	private double deductGst;

	private double bonus;

	private int adviserId;

	private int officialId;

	private boolean isClose;

}
