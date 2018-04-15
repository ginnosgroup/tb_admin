package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface VisaService {

	public int addVisa(VisaDTO visaDto) throws ServiceException;

	public int updateVisa(VisaDTO visaDto) throws ServiceException;

	public int countVisa(String keyword, String startHandlingDate, String endHandlingDate, String startDate,
			String endDate, Integer adviserId) throws ServiceException;

	public List<VisaDTO> listVisa(String keyword, String startHandlingDate, String endHandlingDate,
			String startDate, String endDate, Integer adviserId, int pageNum, int pageSize) throws ServiceException;

	public VisaDTO getVisaById(int id) throws ServiceException;

	public int deleteVisaById(int id) throws ServiceException;

}
