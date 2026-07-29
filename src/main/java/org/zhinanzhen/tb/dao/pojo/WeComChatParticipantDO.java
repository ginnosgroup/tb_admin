package org.zhinanzhen.tb.dao.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class WeComChatParticipantDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String msgId;
    private String chatId;
    private String participantId;
    private Integer participantType;
    private String participantRole;
}
