package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class MaraDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private Date gmtCreate;

	private String name;

	private String phone;

	private String email;

	/** 客户邮件中展示的联系邮箱，对应 b_mara.956email。 */
	private String form956Email;

	private String state;

	private String imageUrl;

	private String signatureData;

	/** Form 956模板文件路径，对应 b_mara.956path。 */
	private String form956Path;

	private int regionId;

}
