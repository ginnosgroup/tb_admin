package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2020/12/21 16:37
 * Description:
 * Version: V1.0
 */
@Data
public class FinanceBankDO {

    private  int id ;

    private String bank;

    private String bsb;

    private String accountNo;

    private String simple;

    private Date gmtCreate;

    private  Date gmtModify;

    private boolean delete;
}
