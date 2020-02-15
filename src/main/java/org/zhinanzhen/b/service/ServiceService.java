package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServiceService {

	public int addService(ServiceDTO serviceDto) throws ServiceException;

	public int updateService(int id, String code) throws ServiceException;

	public List<ServiceDTO> listService() throws ServiceException;

	public ServiceDTO getServiceById(int id) throws ServiceException;

	public int deleteServiceById(int id) throws ServiceException;

}