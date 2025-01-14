package org.zhinanzhen.b.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServiceService {

	public int addService(ServiceDTO serviceDto) throws ServiceException;

	public int updateService(int id, String name, String code, String role) throws ServiceException;

	public int countService(String name, boolean isZx) throws ServiceException;

	public List<ServiceDTO> listService(String name, boolean isZx, int pageNum, int pageSize) throws ServiceException;

	public int countAllService(String name) throws ServiceException;

	public List<ServiceDTO> listAllService(String name, int pageNum, int pageSize) throws ServiceException;

	public ServiceDTO getServiceById(int id) throws ServiceException;

	public int deleteServiceById(int id, boolean isZx) throws ServiceException;

}