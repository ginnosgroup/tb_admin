package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class MessageZanDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private int adminUserId;

	private int messageId;

}
