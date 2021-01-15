package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/01/07 12:11
 * Description:
 * Version: V1.0
 */
@Data
public class ServiceOrderReadcommittedDateDO {

    private int id ;

    private Date gmtModify;

    private Date gmtCreate;

    private int serviceOrderId;

    private Date historyDate;
}
