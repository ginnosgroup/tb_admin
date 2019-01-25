package org.zhinanzhen.tb.service.pojo;

import java.util.Date;
import java.util.List;

import org.zhinanzhen.tb.service.SubjectStateEnum;
import org.zhinanzhen.tb.service.SubjectTypeEnum;

import lombok.Data;

@Data
public class SubjectDTO {

	private int id;
	
	private Date gmtCreate;

	private String name;
	
	private SubjectTypeEnum type;
	
	private int parentId;

	private String logo;

	private double price;

	private Date startDate;

	private Date endDate;

	private SubjectStateEnum state;

	private int categoryId;

	private double preAmount;

	private String codex;

	private String details;

	private String regionIds;

	private int weight;

	private List<SubjectPriceIntervalDTO> priceIntervalList;
	
	private List<OrderDTO> orderList;

}
