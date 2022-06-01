package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ServiceOrderApplicantDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServiceOrderApplicantService {
	
	int addServiceOrderApplicant(ServiceOrderApplicantDTO serviceOrderApplicantDto) throws ServiceException;
	
	List<ServiceOrderApplicantDTO> listServiceOrderApplicant(Integer serviceOrderId, Integer applicantId) throws ServiceException;
	
	int updateServiceOrderApplicant(ServiceOrderApplicantDTO serviceOrderApplicantDto) throws ServiceException;
	
	int deleteServiceOrderApplicant(Integer id) throws ServiceException;

}
