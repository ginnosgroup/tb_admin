package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.List;

public interface VisaOfficialService {
    int addVisa(VisaOfficialDTO visaOfficialDto) throws ServiceException;
    List<VisaOfficialDTO> getVisaOfficialOrder(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate, String endHandlingDate, String state,  String startDate, String endDate, String userName, String applicantName, Integer pageNum, Integer pageSize, Sorter sorter)throws ServiceException;
    int count(Integer officialId,List<Integer> regionIdList, Integer id,String startHandlingDate,String endHandlingDate,  String state, String startDate, String endDate ,String userName,String applicantName) throws ServiceException;

    void update(Integer id,String handling_date,Double commissionAmount,String state) throws ServiceException;
}
