package org.zhinanzhen.b.service.pojo;

import lombok.Data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/14 10:27
 * Description:
 * Version: V1.0
 */

@Data
public class InvoiceDTO {

    //发票id
    private int id;

    //发票id
    private String ids;

    //佣金订单id
    private String order_id;

    //创建时间
    private Date gmt_create;

    //税务发票编号
    private String invoice_no;

    //地区
    private String  branch;

    //税务发票状态
    private String state;

    /**
     * pdf地址
     */
    private String pdfUrl;

}
