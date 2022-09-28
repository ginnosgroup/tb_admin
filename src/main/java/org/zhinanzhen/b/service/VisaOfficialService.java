package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

public interface VisaOfficialService {
    int addVisa(VisaOfficialDTO visaOfficialDto) throws ServiceException;
    List<VisaOfficialDTO> getVisaOfficialOrder(Integer officialId, Integer regionId, Integer id, String startHandlingDate, String endHandlingDate, String commissionState, String startSubmitIbDate, String endSubmitIbDate, String startDate, String endDate, String userName, String applicantName, Integer pageNum, Integer pageSize)throws ServiceException;
    int count(Integer officialId,Integer regionId, Integer id,String startHandlingDate,String endHandlingDate,  String commissionState, String startSubmitIbDate, String endSubmitIbDate, String startDate, String endDate ,String userName,String applicantName) throws ServiceException;

}
