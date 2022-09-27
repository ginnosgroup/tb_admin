package org.zhinanzhen.b.service.impl;


import com.ikasoa.core.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.VisaOfficialDao;
import org.zhinanzhen.b.dao.pojo.VisaOfficialDO;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;

@Service("VisaOfficialService")
public class VisaOfficialServiceImpl extends BaseService implements VisaOfficialService {
    @Resource
    VisaOfficialDao visaOfficialDao;

    @Override
    public int addVisa(VisaOfficialDTO visaOfficialDTO) throws ServiceException {
        if (visaOfficialDTO == null) {
            ServiceException se = new ServiceException("visaOfficialDto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        if (visaOfficialDao.countVisaByServiceOrderIdAndExcludeCode(visaOfficialDTO.getServiceOrderId(), visaOfficialDTO.getCode()) > 0) {
            ServiceException se = new ServiceException("已创建过佣金订单,不能重复创建!");
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        try {
            VisaOfficialDO visaOfficialDO = mapper.map(visaOfficialDTO, VisaOfficialDO.class);
            if (visaOfficialDao.addVisa(visaOfficialDO) > 0) {
                visaOfficialDTO.setId(visaOfficialDO.getId());
                return visaOfficialDO.getId();
            } else {
                return 0;
            }
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }
}
