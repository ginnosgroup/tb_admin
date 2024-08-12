package org.zhinanzhen.b.dao.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnualReport {
    @ExcelProperty(value = "服务佣金订单ID")
    private Integer serviceOrderId;

    @ExcelProperty(value = "客户姓名")
    private String userName;

    @ExcelProperty(value = "申请人姓名")
    private String applicantName;

    @ExcelProperty(value = "创建日期")
    private Date gmtCreat;

    @ExcelProperty(value = "支付币种")
    private String money;

    @ExcelProperty(value = "创建订单时汇率")
    private double exchangeRate;

    @ExcelProperty(value = "收款方式")
    private String moneyThing;

    @ExcelProperty(value = "服务项目")
    private String serviceName;

    @ExcelProperty(value = "总计应收人民币")
    private double zongjiRMB;

    @ExcelProperty(value = "总计应收澳币")
    private double zongjiAOBI;

    @ExcelProperty(value = "总计收款人民币")
    private double zongjishouRMB;

    @ExcelProperty(value = "总计收款澳币")
    private double zongjishouAOBI;

    @ExcelProperty(value = "顾问")
    private String guwen;

    @ExcelProperty(value = "地区")
    private String diqu;
}
