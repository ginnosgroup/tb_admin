package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/19 15:07
 * Description:
 * Version: V1.0
 */
@Data
public class InvoiceServiceFeeDO {

    private int id;

    private String order_id;

    private Date invoiceDate;

    private String email;

    private String company;

    private String abn;

    private String address;

    private String tel;

    private String invoiceNo;

    private String note;

    private String accountname;

    private String bsb;

    private String accountno;

    private String state;

    private String branch;

    private String billTo;

    private String pdfUrl;

    private List<InvoiceServiceFeeDescriptionDO> invoiceServiceFeeDescriptionDOList;
}
