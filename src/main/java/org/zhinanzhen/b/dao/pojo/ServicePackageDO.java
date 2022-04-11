package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ServicePackageDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private String type;

	private int serviceId;

	private int num;
	
	private List<ServicePackagePriceDO> ServicePackagePirceList;

}
