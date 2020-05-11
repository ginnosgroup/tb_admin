package org.zhinanzhen.b.service.pojo;

import org.zhinanzhen.b.dao.pojo.ServiceOrderCommentDO;

import lombok.Getter;
import lombok.Setter;

public class ServiceOrderCommentDTO extends ServiceOrderCommentDO {

	@Getter
	@Setter
	private String adminUserName;

}
