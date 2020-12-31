package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import org.zhinanzhen.tb.dao.pojo.RegionDO;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2020/12/25 15:14
 * Description:
 * Version: V1.0
 */
@Data
public class FinanceBankCodeDTO {

    private  int id ;

    private String bank;

    private String bsb;

    private String accountNo;

    private String simple;

    private Date gmtCreate;

    private  Date gmtModify;

    private boolean delete;

    private RegionDO regionDO;

    private String code;
}
