package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/03/24 10:08
 * Description:
 * Version: V1.0
 */
@Data
public class ChatDO {

    private  int id;

    private Date gmtCreate;

    private Date gmtModify;

    private int serviceOrderId;

    private String chatId;

    private  int userId;

    private  int maraId;

    private  int adviserId;

    private  int officialId;
}
