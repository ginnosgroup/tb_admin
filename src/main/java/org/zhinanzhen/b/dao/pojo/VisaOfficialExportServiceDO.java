package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

/** 导出服务项目所需的职评信息，同一服务订单只返回一条。 */
@Data
public class VisaOfficialExportServiceDO {
    private int serviceOrderId;
    private String serviceName;
    private String serviceCode;
    private String categoryName;
    private String assessName;
    private String completionPhase;

    public String serviceItem() {
        return serviceItem(completionPhase);
    }

    public boolean hasAssessment() {
        return categoryName != null || assessName != null;
    }

    public String serviceItem(String completionPhase) {
        String item = emptyIfNull(serviceName) + "-" + emptyIfNull(serviceCode)
                + "-" + emptyIfNull(categoryName) + "-" + emptyIfNull(assessName);
        return completionPhase == null || completionPhase.isEmpty() ? item : item + "-" + completionPhase;
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}
