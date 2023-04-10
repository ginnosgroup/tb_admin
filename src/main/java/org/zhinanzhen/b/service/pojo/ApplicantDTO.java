package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.ApplicantDO;
@Data
public class ApplicantDTO extends ApplicantDO {

	private String url;


	private String content;
	

	private UserDTO userDto;

	private boolean isSubmitMM;

	private int serviceOrderId;

}
