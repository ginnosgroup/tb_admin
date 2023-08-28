package org.zhinanzhen.b.dao.pojo.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO: 工作详情
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentDetails {

    private String dateFrom;//开始时间

    private String dateTo;//结束时间

    private String status;//Employed在职 Student学生 Retired退休 Self-Employed自雇

    private String position;//岗位

    private String country;//国家：国家和线上选择国家类型一致


}
