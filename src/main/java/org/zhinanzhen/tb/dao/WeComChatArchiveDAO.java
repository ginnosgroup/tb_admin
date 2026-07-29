package org.zhinanzhen.tb.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.WeComChatMessageDO;
import org.zhinanzhen.tb.dao.pojo.WeComChatParticipantDO;
import org.zhinanzhen.tb.dao.pojo.WeComChatSyncStateDO;

import java.util.List;

public interface WeComChatArchiveDAO {

    int upsertMessage(WeComChatMessageDO message);

    int insertMessageParticipant(WeComChatParticipantDO participant);

    int insertChatParticipant(WeComChatParticipantDO participant);

    WeComChatSyncStateDO getSyncState(@Param("syncKey") String syncKey);

    int saveSyncState(@Param("syncKey") String syncKey,
                      @Param("nextCursor") String nextCursor,
                      @Param("hasMore") int hasMore,
                      @Param("syncedCount") int syncedCount,
                      @Param("lastError") String lastError);

    long countAllMessages();

    Long getEarliestMessageTime();

    Long getLatestMessageTime();

    int countDirectMessages(@Param("employeeUserId") String employeeUserId,
                            @Param("externalUserId") String externalUserId,
                            @Param("startTime") long startTime,
                            @Param("endTime") long endTime);

    int countGroupMessages(@Param("employeeUserId") String employeeUserId,
                           @Param("externalUserId") String externalUserId,
                           @Param("startTime") long startTime,
                           @Param("endTime") long endTime);

    List<WeComChatMessageDO> listMessages(
            @Param("employeeUserId") String employeeUserId,
            @Param("externalUserId") String externalUserId,
            @Param("startTime") long startTime,
            @Param("endTime") long endTime,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);
}
