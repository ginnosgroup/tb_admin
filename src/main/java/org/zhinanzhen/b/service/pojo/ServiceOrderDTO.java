package org.zhinanzhen.b.service.pojo;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import lombok.Data;

@Data
public class ServiceOrderDTO {

	private int id;

	private Date gmtCreate;

	private Date finishDate;

	private String type;

	private int serviceId;

	private ServiceDTO service;

	private int schoolId;

	private SchoolDTO school;

	private String state;

	private boolean isSettle;

	private boolean isDepositUser;

	private int subagencyId;

	private SubagencyDTO subagency;

	private boolean isPay;

	private int receiveTypeId;

	private ReceiveTypeDTO receiveType;

	private Date receiveDate;

	private double receivable;

	private double discount;

	private double received;

	private int paymentTimes;

	private double amount;

	private double gst;

	private double deductGst;

	private double bonus;

	private int userId;

	private UserDTO user;

	private int maraId;

	private MaraDTO mara;

	private int adviserId;

	private AdviserDTO adviser;

	private int officialId;

	private OfficialDTO official;

	private String remarks;

	private ServiceOrderReviewDTO review;

	private List<ServiceOrderReviewDTO> reviews;

}
