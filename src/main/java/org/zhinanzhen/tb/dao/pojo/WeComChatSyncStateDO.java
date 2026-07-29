package org.zhinanzhen.tb.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class WeComChatSyncStateDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String syncKey;
    private String nextCursor;
    private Integer hasMore;
    private Long totalSynced;
    private Date lastSyncTime;
    private String lastError;
}
