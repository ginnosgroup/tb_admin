package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ServicePackageDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServicePackageService {

	int add(ServicePackageDTO servicePackageDto) throws ServiceException;
	
	int update(ServicePackageDTO servicePackageDto) throws ServiceException;
	
	int count(Integer serviceId) throws ServiceException;

	List<ServicePackageDTO> list(Integer serviceId, int pageNum, int pageSize) throws ServiceException;

	ServicePackageDTO getById(int id) throws ServiceException;

	int delete(int id) throws ServiceException;

}
