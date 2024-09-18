package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class SysLogDO {

    private long id;

    private String uri;

	private String daoMethodName;
	
    private String ip;

    private String wholeSql;

    private String desc;

    private Date createDate;

    private Integer userId;
}
