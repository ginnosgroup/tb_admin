package org.zhinanzhen.tb.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface WeComChatArchiveService {

    JSONObject syncNow() throws Exception;

    JSONObject syncDateRange(long startTime, long endTime) throws Exception;

    JSONObject getArchiveStatus();

    JSONObject queryMessages(String employeeUserId,
                             String externalUserId,
                             long startTime,
                             long endTime,
                             int pageNum,
                             int pageSize);

    JSONArray listEmployeeGroupChatIds(String employeeUserId);

    JSONObject queryEmployeeGroupMessages(String employeeUserId,
                                          String chatId,
                                          long startTime,
                                          long endTime,
                                          int pageNum,
                                          int pageSize);
}
