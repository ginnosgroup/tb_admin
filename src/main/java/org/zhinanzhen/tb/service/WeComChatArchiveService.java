package org.zhinanzhen.tb.service;

import com.alibaba.fastjson.JSONObject;

public interface WeComChatArchiveService {

    JSONObject syncNow() throws Exception;

    JSONObject getArchiveStatus();

    JSONObject queryMessages(String employeeUserId,
                             String externalUserId,
                             long startTime,
                             long endTime,
                             int pageNum,
                             int pageSize);

    JSONObject queryEmployeeGroupMessages(String employeeUserId,
                                          long startTime,
                                          long endTime,
                                          int pageNum,
                                          int pageSize);
}
