package org.zhinanzhen.tb.service.pojo;

import java.util.Date;

import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.service.AdviserStateEnum;

import lombok.Data;

@Data
public class AdviserDTO {

	private int id;

	private Date gmtCreate;

	private String name;

	private String phone;

	private String email;
	
	private AdviserStateEnum state;

	private String imageUrl;

	private int regionId;
	
	private String regionName;
	
	private RegionDO regionDo;
}
