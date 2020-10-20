package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/15 11:37
 * Description:
 * Version: V1.0
 */
@Data
public class InvoiceCompanyDO {

    //id
    private int id;

    //公司名字
    private String name;

    //简写
    //private String simple;

    //abn
    private String abn;

    private String email;

    private String tel;

    private String bsb;

    //账户
    private String account;

    //标志 SC:留学 SF:servicefee SCSF:留学和servicefee
    //private String flag;

}
