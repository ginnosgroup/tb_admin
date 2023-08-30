package org.zhinanzhen.b.dao.pojo.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class previousStayDetails {
    private String applicants;//曾前往其他国家的申请人姓名

    private String country;//国家
    /**
     * Citizen 公民
     * Permanent Resident 永久居留权
     * Student 学生
     * Visitor/Tourist访客/游客
     * Work Visa工作签证
     * Refugee 难民
     * Illegal Resident非法居留者
     * Asylum Applicant庇护申请人
     * No Legal Status无法律地位
     * Other其他
     */
    private String legalStatus;//入境的身份

    private String arrivalDate;//到达日期(日/月/年

    private String departureDate;//出发日期

    private String reason;//旅行主要原因
    // Visit Family探亲 Visit Friends访友 Business商务 Holiday度假
    // Study学习 Work工作 Medical就医 Temporary Residence临时居留权 Permanent Residence永久居留权
}
