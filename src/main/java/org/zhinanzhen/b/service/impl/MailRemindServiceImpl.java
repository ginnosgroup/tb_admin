package org.zhinanzhen.b.service.impl;

import com.ikasoa.core.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.MailRemindDAO;
import org.zhinanzhen.b.dao.pojo.MailRemindDO;
import org.zhinanzhen.b.service.MailRemindService;
import org.zhinanzhen.b.service.pojo.MailRemindDTO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Resource
    UserDAO userDAO;

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
    public List<MailRemindDTO> list(Integer adviserId, Integer offcialId, Integer kjId,Integer serviceOrderId,
                                    Integer visaId, Integer commissionOrderId,  Integer userId, boolean isToday, boolean isAll) throws ServiceException {

        List<MailRemindDO> mailRemindDOList;
        try {
            mailRemindDOList = mailRemindDAO.list(adviserId,offcialId,kjId,serviceOrderId,visaId,commissionOrderId, userId,isToday,isAll);
        }catch (Exception e){
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
            throw se;
        }
        return convertToDTOList(mailRemindDOList);
    }

    @Override
    public List<MailRemindDTO> listByVisaIds(Integer adviserId, Integer offcialId, Integer kjId,
                                             List<Integer> visaIdList, boolean isToday, boolean isAll) throws ServiceException {
        if (visaIdList == null || visaIdList.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<MailRemindDO> mailRemindDOList = mailRemindDAO.listByVisaIds(adviserId, offcialId, kjId, visaIdList, isToday, isAll);
            return convertToDTOList(mailRemindDOList);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
            throw se;
        }
    }

    private List<MailRemindDTO> convertToDTOList(List<MailRemindDO> mailRemindDOList) {
        List<MailRemindDTO> mailRemindDTOList = new ArrayList<>();
        if (mailRemindDOList == null || mailRemindDOList.isEmpty()) {
            return mailRemindDTOList;
        }

        List<Integer> userIds = mailRemindDOList.stream()
                .map(MailRemindDO::getUserId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> userNameMap = new java.util.HashMap<>();
        for (Integer userId : userIds) {
            UserDO userDO = userDAO.getUserById(userId);
            userNameMap.put(userId, userDO != null ? userDO.getName() : null);
        }

        for (MailRemindDO mailRemindDO: mailRemindDOList){
            MailRemindDTO mailRemindDTO = mapper.map(mailRemindDO,MailRemindDTO.class);
            Integer _userId = mailRemindDO.getUserId();
            if (_userId != null && _userId > 0){
                mailRemindDTO.setUserName(userNameMap.get(_userId));
            }
            mailRemindDTOList.add(mailRemindDTO);
        }
        return mailRemindDTOList;
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
