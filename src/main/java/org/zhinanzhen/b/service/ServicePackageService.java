package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ServicePackageDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServicePackageService {

	int add(ServicePackageDTO servicePackageDto) throws ServiceException;

	List<ServicePackageDTO> list(Integer serviceId) throws ServiceException;

	ServicePackageDTO getById(int id) throws ServiceException;

	int delete(int id) throws ServiceException;

}
