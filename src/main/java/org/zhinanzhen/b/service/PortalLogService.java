package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.PortalLogDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface PortalLogService {

	int addPortalLog(PortalLogDTO portalLogDto) throws ServiceException;

	List<PortalLogDTO> listPortalLog(Integer portalId, int pageNum, int pageSize) throws ServiceException;

	int countPortalLog(Integer portalId) throws ServiceException;

}
