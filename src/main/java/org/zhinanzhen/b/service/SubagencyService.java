package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.SubagencyDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface SubagencyService {

	public int addSubagency(SubagencyDTO subagencyDto) throws ServiceException;

	public int updateSubagency(int id, String name, String country, double commissionRate) throws ServiceException;

	public List<SubagencyDTO> listSubagency(String keyword) throws ServiceException;

	public SubagencyDTO getSubagencyById(int id) throws ServiceException;

	public int deleteSubagencyById(int id) throws ServiceException;

    String getSubagencyByServiceOrderId(int serviceOrderId);
}
