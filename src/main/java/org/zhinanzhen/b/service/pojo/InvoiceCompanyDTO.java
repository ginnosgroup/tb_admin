package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.b.dao.pojo.InvoiceCompanyDO;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/15 14:13
 * Description:
 * Version: V1.0
 */
@Data
public class InvoiceCompanyDTO extends InvoiceCompanyDO {

    //id
    private int id;

    //公司名字
    private String name;

    //abn
    private String abn;

    private String email;

    private String tel;

    private String bsb;

    //账户
    private String account;

    //标志 SC:留学 SF:servicefee SCSF:留学和servicefee
    //private String flag;
    private String invoiceNo;

    private String invoiceDate;

    private String simple;
    //地址
    private String address;
}
