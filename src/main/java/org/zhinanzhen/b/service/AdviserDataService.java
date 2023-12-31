package org.zhinanzhen.b.service;

import java.util.List;
import java.util.Map;

import org.zhinanzhen.b.service.pojo.AdviserCommissionOrderDTO;
import org.zhinanzhen.b.service.pojo.AdviserServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.AdviserUserDTO;
import org.zhinanzhen.b.service.pojo.AdviserVisaDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface AdviserDataService {

	List<AdviserServiceOrderDTO> listServiceOrder(Integer adviserId) throws ServiceException;

	List<AdviserVisaDTO> listVisa(Integer adviserId) throws ServiceException;

	List<AdviserCommissionOrderDTO> listCommissionOrder(Integer adviserId) throws ServiceException;

	List<AdviserUserDTO> listUser(Integer adviserId) throws ServiceException;
	
	Map<String, Integer> adviserDataMigration(Integer newAdviserId, Integer adviserId, Integer userId) throws ServiceException;

}
