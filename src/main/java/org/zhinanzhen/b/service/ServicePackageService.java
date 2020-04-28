package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ServicePackageDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServicePackageService {

	public ServicePackageDTO getById(int id) throws ServiceException;

	public List<ServicePackageDTO> list() throws ServiceException;

}
