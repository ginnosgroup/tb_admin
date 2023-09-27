package org.zhinanzhen.b.dao.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderExportDTO {
    @ExcelProperty("id")
    private int id;

    @ExcelProperty("办理完成时间")
    private String finishDate;

    @ExcelProperty("提交审核时间")
    private String officialApprovalDate;

    @ExcelProperty("提交申请时间")
    private String readcommittedDate;

    @ExcelProperty("客户姓名")
    private String userName;

    @ExcelProperty("申请人姓名")
    private String applicantName;

    @ExcelProperty("申请人生日")
    private String applicantBirthday;

    @ExcelProperty("手机号码")
    private String phone;

    @ExcelProperty("所属顾问")
    private String adviserName;

    @ExcelProperty("文案")
    private String officialName;

    @ExcelProperty("Mara")
    private String maraName;

    @ExcelProperty("服务项目")
    private String serviceCodeAndName;

    @ExcelProperty("状态")
    private String state;

}
