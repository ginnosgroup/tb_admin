package org.zhinanzhen.b.service.impl;

import com.ikasoa.core.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.MailRemindDAO;
import org.zhinanzhen.b.dao.pojo.MailRemindDO;
import org.zhinanzhen.b.service.MailRemindService;
import org.zhinanzhen.b.service.pojo.MailRemindDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/06/16 下午 3:53
 * Description:
 * Version: V1.0
 */
@Service
public class MailRemindServiceImpl extends BaseService implements MailRemindService{

    @Resource
    MailRemindDAO mailRemindDAO;

    @Override
    public int add(MailRemindDTO mailRemindDTO) throws ServiceException {
        if (mailRemindDTO == null) {
            ServiceException se = new ServiceException("remindDto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        MailRemindDO mailRemindDO  = mapper.map(mailRemindDTO,MailRemindDO.class);
        if (mailRemindDAO.add(mailRemindDO) > 0 )
            return mailRemindDO.getId();
        else
            return 0;
    }

    @Override
    public List<MailRemindDTO> list(Integer adviserId, Integer offcialId, Integer serviceOrderId,
                                    Integer visaId, Integer commissionOrderId,  boolean isToday) throws ServiceException {

        List<MailRemindDTO> MailRemindDTOList = new ArrayList<>();
        List<MailRemindDO> MailRemindDOList = null;
        try {
            MailRemindDOList = mailRemindDAO.list(adviserId,offcialId,serviceOrderId,visaId,commissionOrderId, isToday,true);
            if (MailRemindDOList == null)
                return null;
        }catch (Exception e){
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
            throw se;
        }
        MailRemindDOList.forEach(MailRemindDO ->{
            MailRemindDTOList.add(mapper.map(MailRemindDO,MailRemindDTO.class));
        });
        return MailRemindDTOList;
    }

    @Override
    public int delete(int id, Integer adviserId, Integer offcialId) {
        return mailRemindDAO.delete(id,adviserId,offcialId);
    }

    @Override
    public MailRemindDTO getByid(int id) {
        return mapper.map(mailRemindDAO.getByid(id),MailRemindDTO.class);
    }

    @Override
    public int update(MailRemindDTO mailRemindDTO) throws ServiceException {
        if (mailRemindDTO == null){
            ServiceException se = new ServiceException("MailRemindDTO is null");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        MailRemindDO mailRemindDO = mapper.map(mailRemindDTO,MailRemindDO.class);
        return mailRemindDAO.update(mailRemindDO);
    }



}
