package org.zhinanzhen.tb.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.tb.dao.pojo.WeComChatMessageDO;
import org.zhinanzhen.tb.dao.pojo.WeComChatParticipantDO;
import org.zhinanzhen.tb.dao.pojo.WeComChatSyncStateDO;

import java.util.List;

public interface WeComChatArchiveDAO {

    int upsertMessage(WeComChatMessageDO message);

    int batchUpsertMessages(@Param("list") List<WeComChatMessageDO> messages);

    int insertMessageParticipant(WeComChatParticipantDO participant);

    int batchInsertMessageParticipants(
            @Param("list") List<WeComChatParticipantDO> participants);

    int insertChatParticipant(WeComChatParticipantDO participant);

    int batchInsertChatParticipants(
            @Param("list") List<WeComChatParticipantDO> participants);

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

    int countEmployeeGroupMessages(
            @Param("employeeUserId") String employeeUserId,
            @Param("startTime") long startTime,
            @Param("endTime") long endTime);

    List<WeComChatMessageDO> listMessages(
            @Param("employeeUserId") String employeeUserId,
            @Param("externalUserId") String externalUserId,
            @Param("startTime") long startTime,
            @Param("endTime") long endTime,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    List<WeComChatMessageDO> listEmployeeGroupMessages(
            @Param("employeeUserId") String employeeUserId,
            @Param("startTime") long startTime,
            @Param("endTime") long endTime,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);
}
