package org.zhinanzhen.b.dao;

import org.zhinanzhen.b.dao.pojo.CommissionOrderTempDO;
import org.zhinanzhen.b.service.pojo.CommissionOrderTempDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/10/21 下午 4:23
 * Description:
 * Version: V1.0
 */
public interface CommissionOrderTempDAO {

    int addCommissionOrderTemp(CommissionOrderTempDO commissionOrderTempDO);

    CommissionOrderTempDO getCommissionOrderTempByServiceOrderId(int id);

    List<CommissionOrderTempDO> getCommissionOrderTempByVerifyCode(String verifyCode);

    int update(CommissionOrderTempDO commissionOrderTempDO);
}
