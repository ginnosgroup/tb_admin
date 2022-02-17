package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ServicePackagePriceDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ServicePackagePriceService {

	int addServicePackagePrice(ServicePackagePriceDTO servicePackagePriceDto) throws ServiceException;

	int updateServicePackagePrice(ServicePackagePriceDTO servicePackagePriceDto) throws ServiceException;

	List<ServicePackagePriceDTO> listServicePackagePrice(int servicePackageId, int regionId) throws ServiceException;

	ServicePackagePriceDTO getServicePackagePriceById(int id) throws ServiceException;

	void deleteById(int id) throws ServiceException;

}
