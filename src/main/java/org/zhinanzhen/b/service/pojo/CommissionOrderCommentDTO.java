package org.zhinanzhen.b.service.pojo;

import org.zhinanzhen.b.dao.pojo.CommissionOrderCommentDO;

import lombok.Getter;
import lombok.Setter;

public class CommissionOrderCommentDTO extends CommissionOrderCommentDO {

	@Getter
	@Setter
	private String adminUserName;

}
