package org.zhinanzhen.b.service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewBalanceDTO {
    // 签证支付总金额（不含GST）
    private Double visaAggregateAmount;
    // 免费订单支出
    private Double freeOrderExpenditure;
    // 目前可用余额
    private Double availableBalance;
    // 留学预收业绩
    private Double ovstAdvanceReceipts;
    // 留学待支付金额
    private Double ovstUnpaidAmount;
}
