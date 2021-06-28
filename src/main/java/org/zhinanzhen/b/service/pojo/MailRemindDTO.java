package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/06/16 下午 4:37
 * Description:
 * Version: V1.0
 */
@Data
public class MailRemindDTO {
    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private String code;

    private String mail;

    private String title;

    private String content;

    private Date sendDate;

    private Integer serviceOrderId;

    private Integer visaId;

    private Integer commissionOrderId;

    private Integer adviserId;

    private Integer offcialId;

    private Integer userId;
}
