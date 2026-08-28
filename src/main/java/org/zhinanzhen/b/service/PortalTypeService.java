package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.PortalTypeDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface PortalTypeService {

	int addPortalType(PortalTypeDTO portalTypeDto) throws ServiceException;

	int updatePortalType(PortalTypeDTO portalTypeDto) throws ServiceException;

	List<PortalTypeDTO> listPortalType(Integer isDelete, String keyword) throws ServiceException;

	PortalTypeDTO getPortalType(Integer id) throws ServiceException;

	int deletePortalType(int id) throws ServiceException;

}
