package org.zhinanzhen.tb.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class WeComChatMessageDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Date gmtCreate;
    private Date gmtModify;
    private String msgId;
    private Long sendTimeEpochMillis;
    private String senderJson;
    private String receiverJson;
    private String chatId;
    private String msgType;
    private String secretKey;
    private String conversationType;
}
