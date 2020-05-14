package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.VisaCommentDTO;
import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface VisaService {

	public int addVisa(VisaDTO visaDto) throws ServiceException;

	public int updateVisa(VisaDTO visaDto) throws ServiceException;

	public int countVisa(String keyword, String startHandlingDate, String endHandlingDate, List<String> stateList,
			List<String> commissionStateList, String startDate, String endDate, Integer adviserId, Integer userId)
			throws ServiceException;

	public List<VisaDTO> listVisa(String keyword, String startHandlingDate, String endHandlingDate,
			List<String> stateList, List<String> commissionStateList, String startDate, String endDate,
			Integer adviserId, Integer userId, int pageNum, int pageSize) throws ServiceException;

	public VisaDTO getVisaById(int id) throws ServiceException;

	public int deleteVisaById(int id) throws ServiceException;

	int addComment(VisaCommentDTO visaCommentDto) throws ServiceException;

	List<VisaCommentDTO> listComment(int id) throws ServiceException;

	int deleteComment(int id) throws ServiceException;

}
