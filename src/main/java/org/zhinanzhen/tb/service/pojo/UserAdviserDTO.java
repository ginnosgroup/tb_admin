package org.zhinanzhen.tb.service.pojo;

import org.zhinanzhen.tb.dao.pojo.UserAdviserDO;

import lombok.Getter;
import lombok.Setter;

public class UserAdviserDTO extends UserAdviserDO {

	private static final long serialVersionUID = 1L;
	
	@Getter
	@Setter
	private AdviserDTO adviserDto;

}
