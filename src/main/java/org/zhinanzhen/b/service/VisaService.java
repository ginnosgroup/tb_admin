package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.VisaCommentDTO;
import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.b.service.pojo.VisaReportDTO;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.service.ServiceException;

public interface VisaService {

	public int addVisa(VisaDTO visaDto) throws ServiceException;

	public int updateVisa(VisaDTO visaDto) throws ServiceException;

	public int countVisa(Integer id, String keyword, String startHandlingDate, String endHandlingDate,
			List<String> stateList, List<String> commissionStateList, String startKjApprovalDate,
			String endKjApprovalDate, String startDate, String endDate, String startInvoiceCreate,
			String endInvoiceCreate, List<Integer> regionIdList, Integer adviserId, Integer userId,
			String applicantName, String state) throws ServiceException;

	public List<VisaDTO> listVisa(Integer id, String keyword, String startHandlingDate, String endHandlingDate,
			List<String> stateList, List<String> commissionStateList, String startKjApprovalDate,
			String endKjApprovalDate, String startDate, String endDate, String startInvoiceCreate,
			String endInvoiceCreate, List<Integer> regionIdList, Integer adviserId, Integer userId, String userName,
			String applicantName, String state, int pageNum, int pageSize, Sorter sorter) throws ServiceException;

	public List<VisaReportDTO> listVisaReport(String startDate, String endDate, String dateType, String dateMethod,
			Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException;
	public VisaDTO getVisaById(int id) throws ServiceException;

	public VisaDTO getFirstVisaByServiceOrderId(int serviceOrderId) throws ServiceException;

	public int deleteVisaById(int id) throws ServiceException;

	int addComment(VisaCommentDTO visaCommentDto) throws ServiceException;

	List<VisaCommentDTO> listComment(int id) throws ServiceException;

	int deleteComment(int id) throws ServiceException;
	
	void sendRefuseEmail(VisaDTO visaDto);
	List<VisaDTO> getCommissionOrder(Integer officialId, Integer regionId,Integer id,String startHandlingDate,String endHandlingDate, String commissionState, String startSubmitIbDate, String endSubmitIbDate, String startDate, String endDate,String userName,String applicantName,Integer pageNum, Integer pageSize);
	int count(Integer officialId,Integer regionId, Integer id,String startHandlingDate,String endHandlingDate,  String commissionState, String startSubmitIbDate, String endSubmitIbDate, String startDate, String endDateuserName,String userName,String applicantName) throws ServiceException;

}
