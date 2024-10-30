package org.zhinanzhen.b.service;

import org.apache.ibatis.annotations.Delete;
import org.zhinanzhen.b.dao.pojo.VisaOfficialDO;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.Date;
import java.util.List;

public interface VisaOfficialService {
    int addVisa(VisaOfficialDTO visaOfficialDto) throws ServiceException;
    List<VisaOfficialDTO> listVisaOfficialOrder(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate,
                                                String endHandlingDate, String state,  String startDate, String endDate,
                                                String firstSettlementMonth,String lastSettlementMonth, String userName,
                                                String applicantName, Boolean isMerged, Integer pageNum, Integer pageSize,
                                                Sorter sorter, String serviceOrderType, String currency)throws ServiceException;
    
    VisaOfficialDTO getByServiceOrderId(Integer serviceOrderId)throws ServiceException;
    
    int count(Integer officialId,List<Integer> regionIdList, Integer id,String startHandlingDate,String endHandlingDate,  String state, String startDate, String endDate ,String userName,String applicantName, Boolean isMerged, String currency) throws ServiceException;

    void update(Integer id,String handling_date,Double commissionAmount,String state, Integer serviceId) throws ServiceException;
    
	void updateMerged(Integer id, Boolean isMerged) throws ServiceException;

    List<VisaOfficialDO> monthlyStatement();

    int deleteById(Integer id);
//    VisaOfficialDO buildVisa(VisaOfficialDTO visaOfficialDTO) throws ServiceException;

    List<VisaOfficialDTO> getAllvisaOfficialByServiceOrderId(Integer serviceOrderId);

    VisaOfficialDO getByServiceOrderIdOne(int id);

    void visaServiceupdateHandlingDate(int id, Date handlingDate);

    void visaServiceupdateVisaOfficial(VisaOfficialDO visaOfficialDO1);

}
