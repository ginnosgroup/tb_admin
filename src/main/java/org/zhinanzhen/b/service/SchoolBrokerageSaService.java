package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.SchoolBrokerageSaDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface SchoolBrokerageSaService {

	public int addSchoolBrokerageSa(SchoolBrokerageSaDTO schoolBrokerageSaDto) throws ServiceException;

	public int updateSchoolBrokerageSa(SchoolBrokerageSaDTO schoolBrokerageSaDto) throws ServiceException;

	public int countSchoolBrokerageSa(String keyword, String startHandlingDate, String endHandlingDate,
			String startDate, String endDate, Integer adviserId, Integer schoolId, Integer subagencyId,
			Boolean isSettleAccounts) throws ServiceException;

	public List<SchoolBrokerageSaDTO> listSchoolBrokerageSa(String keyword, String startHandlingDate,
			String endHandlingDate, String startDate, String endDate, Integer adviserId, Integer schoolId,
			Integer subagencyId, Boolean isSettleAccounts, int pageNum, int pageSize) throws ServiceException;
	
	public int updateClose(int id, boolean isClose) throws ServiceException;

	public SchoolBrokerageSaDTO getSchoolBrokerageSaById(int id) throws ServiceException;

	public int deleteSchoolBrokerageSaById(int id) throws ServiceException;

}
