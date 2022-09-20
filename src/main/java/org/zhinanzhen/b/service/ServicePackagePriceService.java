package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.dao.pojo.ServicePackagePriceDO;
import org.zhinanzhen.b.service.pojo.ServicePackagePriceDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServicePackagePriceService {

	int addServicePackagePrice(ServicePackagePriceDTO servicePackagePriceDto) throws ServiceException;

	int updateServicePackagePrice(ServicePackagePriceDTO servicePackagePriceDto) throws ServiceException;

	int countServicePackagePrice(int serviceId, int regionId) throws ServiceException;

	List<ServicePackagePriceDTO> listServicePackagePrice(int serviceId, int regionId, int pageNum, int pageSize)
			throws ServiceException;

	ServicePackagePriceDTO getServicePackagePriceById(int id) throws ServiceException;

	ServicePackagePriceDO getServicePackagePriceByServiceId(int id) throws ServiceException ;

	void deleteById(int id) throws ServiceException;

}
