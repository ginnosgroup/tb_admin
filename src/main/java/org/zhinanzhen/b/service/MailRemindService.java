package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.MailRemindDTO;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/06/16 下午 3:53
 * Description:
 * Version: V1.0
 */
public interface MailRemindService {

    int add(MailRemindDTO mailRemindDTO) throws ServiceException;

    List<MailRemindDTO> list(Integer adviserId, Integer offcialId,
                             Integer kjId,
                             Integer serviceOrderId, Integer visaId,
                             Integer commissionOrderId, Integer userId,
                             boolean isToday, boolean isAll) throws ServiceException;

    int delete(int id, Integer adviserId, Integer offcialId);

    MailRemindDTO getByid(int id);

    int update(MailRemindDTO mailRemindDTO) throws ServiceException;
}
