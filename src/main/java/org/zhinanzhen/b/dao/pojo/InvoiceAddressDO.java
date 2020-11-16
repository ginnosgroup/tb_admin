package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/15 11:10
 * Description:
 * Version: V1.0
 */
@Data
public class InvoiceAddressDO {

    private int id;

    //地区
    private String branch;

    //地区对应的地址
    private String address;

    //bsb
    private String  bsb;

    //账户
    private String account;
}
