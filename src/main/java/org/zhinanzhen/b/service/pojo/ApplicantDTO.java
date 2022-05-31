package org.zhinanzhen.b.service.pojo;

import org.zhinanzhen.b.dao.pojo.ApplicantDO;

import lombok.Getter;
import lombok.Setter;

public class ApplicantDTO extends ApplicantDO {

	@Getter
	@Setter
	private String url;

	@Getter
	@Setter
	private String content;

}
