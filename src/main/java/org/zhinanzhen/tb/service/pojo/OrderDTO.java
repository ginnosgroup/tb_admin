package org.zhinanzhen.tb.service.pojo;
import java.util.Date;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.OrderPayTypeEnum;
import org.zhinanzhen.tb.service.OrderStateEnum;

import lombok.Data;
@Data
public class OrderDTO {

    private int id;

    private Date gmtCreate;

    private String name;

    private OrderStateEnum state;

    private int subjectId;

    private int num;

    private double amount;

    private OrderPayTypeEnum payType;

    private String payCode;

    private double payAmount;

    private Date payDate;

    private double createPrice;

    private double finishPrice;

    private int userId;

    private int introducerUserId;

    private int adviserId;

    private Date adviserDate;

    private int regionId;
    
    private double remainPayAmount;
    
    private Date remainPayDate;
    
    private double remainPayBalance;
    
    private double finalPayAmount;

    private UserDO userDo;

    private UserDO introducerDo;

    private SubjectDTO subjectDto;

    private RegionDO regionDo;

    private AdviserDO adviserDo;
}
