package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import org.zhinanzhen.b.service.KjStateEnum;
import org.zhinanzhen.tb.dao.pojo.RegionDO;

import lombok.Data;

@Data
public class KjDTO {

	private int id;

	private Date gmtCreate;

	private String name;

	private String phone;

	private String email;

	private KjStateEnum state;

	private String imageUrl;

	private int regionId;

	private String regionName;

	private RegionDO regionDo;
}
