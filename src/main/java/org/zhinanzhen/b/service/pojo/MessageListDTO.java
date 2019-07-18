package org.zhinanzhen.b.service.pojo;

import lombok.Getter;
import lombok.Setter;

public class MessageListDTO extends MessageDTO {

	@Getter
	@Setter
	private String adminUserName;

	@Getter
	@Setter
	private int zan;

}
