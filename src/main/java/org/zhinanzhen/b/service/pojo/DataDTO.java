package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.CommissionOrderReportDO;
@Data
public class DataDTO {

    private String date;

    private int regionId;

    private String area;

    private int adviserId;

    private String consultant;

    //提前扣拥:确认预收业绩,如果确认预收业绩为null统计预收业绩
    private double deductionCommission;

    //非提前扣拥预收业绩
    private double claimCommission;

    //YZY预收业绩
    private double claimedCommission;

    //学校支付金额
    private double adjustments;

    private double serviceFee;

    private double total;

    public DataDTO(String date, int regionId, String area, int adviserId, String consultant, double serviceFee) {
        this.date = date;
        this.regionId = regionId;
        this.area = area;
        this.adviserId = adviserId;
        this.consultant = consultant;
        this.serviceFee = serviceFee;
    }

    public DataDTO(String date, int regionId, String area, double serviceFee,double deductionCommission, double claimCommission,
                   double claimedCommission, double adjustments) {
        this.date = date;
        this.area = area;
        this.deductionCommission = deductionCommission;
        this.claimCommission = claimCommission;
        this.claimedCommission = claimedCommission;
        this.adjustments = adjustments;
        this.serviceFee = serviceFee;
        this.regionId = regionId;
    }

    public DataDTO() {
    }
}
