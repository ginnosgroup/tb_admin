package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;
//todo 目前在澳大利亚逗留的详细信息
@Data
public class currentStayDetails {
    private String applicants;//包含的申请人姓名

    private String arrivalDate;//到达澳大利亚日期 日/月/年

    private String departureDate;//出发日期 必填

    private String arrivalCity;//澳大利亚入境城市

    private String visaUsed; // 使用的签证

    private String reason;//签证主要原因
    // Visit Family探亲 Visit Friends访友 Business商务 Holiday度假
    // Study学习 Work工作 Medical就医 Temporary Residence临时居留权 Permanent Residence永久居留权

}
