package org.zhinanzhen.b.service.pojo;

import lombok.Data;

@Data
public class CommissionAmountDTO {

    private double commissionAmount;

    private double commission;

    private double refund;

    private double amount;

    private double thirdPrince;

    private String calculation;

    private int ruler;

    private double predictCommissionAmount;//计入佣金提点金额(预估)

    private boolean chinaFixedAmount;

}
