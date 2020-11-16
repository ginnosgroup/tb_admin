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

    private double deductionCommission;

    private double claimCommission;

    private double claimedCommission;

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

    public DataDTO(String date, String area, double serviceFee,double deductionCommission, double claimCommission, double claimedCommission) {
        this.date = date;
        this.area = area;
        this.deductionCommission = deductionCommission;
        this.claimCommission = claimCommission;
        this.claimedCommission = claimedCommission;
        this.serviceFee = serviceFee;
    }

    public DataDTO() {
    }
}
