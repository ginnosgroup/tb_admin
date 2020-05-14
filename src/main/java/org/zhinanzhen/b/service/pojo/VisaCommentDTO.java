package org.zhinanzhen.b.service.pojo;

import org.zhinanzhen.b.dao.pojo.VisaCommentDO;

import lombok.Getter;
import lombok.Setter;

public class VisaCommentDTO extends VisaCommentDO {

	@Getter
	@Setter
	private String adminUserName;

}
