package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import org.zhinanzhen.b.service.OfficialStateEnum;
import org.zhinanzhen.tb.dao.pojo.RegionDO;

import lombok.Data;

@Data
public class OfficialDTO {

	private int id;

	private Date gmtCreate;

	private String name;

	private String phone;

	private String email;

	private OfficialStateEnum state;

	private String imageUrl;

	private int regionId;

	private RegionDO regionDo;
}
