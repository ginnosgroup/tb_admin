package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.MailRemindDO;
import org.zhinanzhen.b.service.pojo.MailRemindDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/06/16 下午 3:54
 * Description:
 * Version: V1.0
 */
public interface MailRemindDAO {

    int add(MailRemindDO mailRemindDO);

    List<MailRemindDO> list(@Param("adviserId") Integer adviserId, @Param("offcialId") Integer offcialId,
                            @Param("serviceOrderId") Integer serviceOrderId, @Param("visaId") Integer visaId,
                            @Param("commissionOrderId") Integer commissionOrderId, @Param("isToday")boolean isToday);

    int delete(@Param("id") int id,@Param("adviserId") Integer adviserId, @Param("offcialId") Integer offcialId);

    MailRemindDO getByid(int id);

    int update(MailRemindDO mailRemindDO);

    List<MailRemindDO> listBySendDate(@Param("type")String type);
}
