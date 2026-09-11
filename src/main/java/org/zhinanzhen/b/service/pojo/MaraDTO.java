package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.b.service.MaraStateEnum;

import lombok.Data;

@Data
public class MaraDTO {

	private int id;

	private Date gmtCreate;

	private String name;

	private String phone;

	private String email;

	private MaraStateEnum state;

	private String imageUrl;

	private String signatureData;

	/** Form 956模板文件路径，对应 b_mara.956path。 */
	private String form956Path;

	private int regionId;

	private String regionName;

	private RegionDO regionDo;

}
