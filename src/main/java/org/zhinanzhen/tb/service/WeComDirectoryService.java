package org.zhinanzhen.tb.service;

import com.alibaba.fastjson.JSONArray;

public interface WeComDirectoryService {

    JSONArray listDepartments() throws Exception;

    JSONArray listEmployees(long departmentId) throws Exception;

    JSONArray listCustomers(String weComUserId) throws Exception;
}
