package org.zhinanzhen.b.service.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ServicePackageDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private String type;

	private int serviceId;

	private String serviceCode;

	private String serviceName;

	private int num;

}
