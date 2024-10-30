package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.service.pojo.WebLogDTO;
import org.zhinanzhen.b.dao.WebLogDAO;
import org.zhinanzhen.b.service.WebLogService;

import javax.annotation.Resource;
import java.util.List;

@Service("WebLogService")
public class WebLogServiceImpl implements WebLogService {

    @Resource
    private WebLogDAO webLogDAO;

    @Override
    public List<WebLogDTO> listByServiceOrderId(Integer serviceOrderId, Integer offset, Integer rows) {
        return webLogDAO.listWebLogs(serviceOrderId, offset, rows);
    }

    @Override
    public Integer count(Integer serviceOrderId) {
        return webLogDAO.count(serviceOrderId);
    }
}
