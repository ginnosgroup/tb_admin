package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface PortalService {

	int addPortal(PortalDTO portalDto) throws ServiceException;

	int updatePortal(PortalDTO portalDto) throws ServiceException;

	List<PortalDTO> listPortal(Integer typeId, String strState, String keyword, int pageNum, int pageSize,
			Integer adviserId, Integer adviserRegionId, Integer officialId, Integer officialRegionId, Integer maraId)
			throws ServiceException;

	int countPortal(Integer typeId, String strState, String keyword, Integer adviserId, Integer adviserRegionId,
			Integer officialId, Integer officialRegionId, Integer maraId) throws ServiceException;

	PortalDTO getPortal(Integer id, Integer adviserId, Integer adviserRegionId, Integer officialId,
			Integer officialRegionId, Integer maraId) throws ServiceException;

	int deletePortal(int id) throws ServiceException;

}
