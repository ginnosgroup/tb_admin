package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ServiceDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	
	private String name;

	private String code;

	private String role;

	private boolean isZx;
	
	private boolean isDelete;

	private boolean settlementNew;

	private boolean isLongTime; // 判断是否为长期签证
	
	private List<ServicePackagePriceDO> ServicePackagePirceList;
}
