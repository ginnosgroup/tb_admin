package org.zhinanzhen.b.dao.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @Author: HL
 * @CreateTime: 2022-09-28
 * @Description: TODO
 * @Version: 1.0
 */
public class VisaOfficialListDO extends VisaOfficialDO {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String userName;

    @Getter
    @Setter
    private Date birthday;

    @Getter
    @Setter
    private String phone;

    @Getter
    @Setter
    private int applicantId;
}
